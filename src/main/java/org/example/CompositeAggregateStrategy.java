package org.example;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.example.data.CompositeData;
import org.example.data.DetailData;
import org.example.data.MasterData;

public class CompositeAggregateStrategy implements AggregationStrategy {

  // region Fields

  // endregion

  // region Methods

  @Override public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

    if (oldExchange == null) {
      CompositeData compositeData = new CompositeData();
      compositeData.setMaster(newExchange.getIn().getBody(MasterData.class));

      newExchange.getIn().setBody(compositeData);

      return newExchange;
    }

    CompositeData oldData = oldExchange.getIn().getBody(CompositeData.class);
    DetailData detail = newExchange.getIn().getBody(DetailData.class);

    oldData.getDetails().add(detail);
    oldExchange.getIn().setBody(oldData);

    return oldExchange;
  }
  // endregion
}
