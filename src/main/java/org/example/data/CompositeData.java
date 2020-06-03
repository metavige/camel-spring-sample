package org.example.data;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
public class CompositeData {
  // region Fields

  private MasterData master;

  private List<DetailData> details = new LinkedList<>();

  // endregion

  // region Methods

  // endregion
}

