package org.example;

import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.ssh.SshResult;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.BootstrapWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RunWith(CamelSpringRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
public class SshTest extends CamelTestSupport {
  // region Fields

  // endregion

  // region Methods

  @Before
  public void onSetUp() throws Exception {

    context.addRoutes(new RouteBuilder() {

      @Override public void configure() throws Exception {

        // 這邊借用了 multipass 的機制
        // 需要先建立了一個虛擬環境以及使用 cloud-init 的設定方式，建立好一組 ssh key
        // 這邊直接使用 (至於 IP，先行寫死，啟動好 multipass 虛擬機之後，再行取得)
        from("direct:in")
          .to("ssh:ubuntu@192.168.64.8?certResource=classpath:test_rsa&timeout=5000")
          .convertBodyTo(String.class, "UTF-8")
          .to("mock:out");

        // 最後的 convertBodyTo 是將 SSH 的輸出 (OutputStream) 轉換成 String
      }
    });
  }

  @Test
  public void testCallSsh() throws Exception {
    // 1. Assign
    String message = "Hello World\n";
    MockEndpoint mockEndpoint = getMockEndpoint("mock:out");
    mockEndpoint.expectedBodiesReceived(message);
    mockEndpoint.expectedHeaderReceived(SshResult.EXIT_VALUE, 0);
//    mockEndpoint.expectedMessagesMatches(m -> {
//      Message outputMess = m.getMessage();
//      log.debug("return ssh result: {}", outputMess.getBody().toString());
//      assertEquals(message, outputMess.getBody());
//
//      // For Debug, Print All Headers
//      //      outputMess.getHeaders().forEach((k, v) -> {
//      //        log.info("Header: {} -> {}", k, v.toString());
//      //      });
//
//      // 執行命令的 ExitCode 應該是 0 (表示正確結束)
//      assertEquals(0, outputMess.getHeader(SshResult.EXIT_VALUE));
//
//      return true;
//    });

    //    mockEndpoint.expectedBodiesReceived(message);

    // 2. Act
    template.sendBody("direct:in", "echo 'Hello World'");
    //    template.sendBody("direct:in", "features:list&#10;");

    // 3. Assert
    assertMockEndpointsSatisfied();
  }

  @Test
  public void testRunError() throws Exception {

    // 1. Assign
    MockEndpoint mockEndpoint = getMockEndpoint("mock:out");
    // 執行命令的 ExitCode 應該是 127 (表示指令找不到 command not found)
    mockEndpoint.expectedHeaderReceived(SshResult.EXIT_VALUE, 127);
    mockEndpoint.expectedMessagesMatches(m -> {
      Message outputMess = m.getMessage();

//      // 執行命令的 ExitCode 不應該是 0
//      assertNotEquals(0, outputMess.getHeader(SshResult.EXIT_VALUE));

      // 印出錯誤訊息
      ByteArrayInputStream errorOutputStream = outputMess.getHeader(SshResult.STDERR, ByteArrayInputStream.class);
      try {
        String errors = convertToString(errorOutputStream);
        log.error(errors);
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }

      return true;
    });

    // 2. Act
    template.sendBody("direct:in", "hello");

    // 3. Assert
    assertMockEndpointsSatisfied();
  }

  @Test
  public void runLongTimeCommand() throws InterruptedException {
    // 1. Assign
    MockEndpoint mockEndpoint = getMockEndpoint("mock:out");
    // 執行命令的 ExitCode 應該是 0 (表示正確結束)
    mockEndpoint.expectedHeaderReceived(SshResult.EXIT_VALUE, 0);

    // 2. Act

    // 這邊執行一個很長時間的指令，看會怎樣
    template.sendBody("direct:in", "for i in {1..15}; do echo \"$i\"; sleep 1; done");

    // 3. Assert
    assertMockEndpointsSatisfied();
  }

  private String convertToString(ByteArrayInputStream is) throws IOException {

    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = is.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }
    // StandardCharsets.UTF_8.name() > JDK 7
    return result.toString("UTF-8");
  }
  // endregion
}
