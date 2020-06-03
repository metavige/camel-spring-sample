package org.example;
 
import org.apache.camel.builder.RouteBuilder;

public class MyRouter extends RouteBuilder  {

	@Override
	public void configure() throws Exception {
 
		from("direct:input")
			.to("direct:output");
	}

}
