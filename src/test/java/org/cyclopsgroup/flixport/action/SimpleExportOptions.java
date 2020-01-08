package org.cyclopsgroup.flixport.action;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@AutoValue
abstract class SimpleExportOptions implements ExportOptions {
  static SimpleExportOptions forDestination(@Nullable String dir, String name) {
    return new AutoValue_SimpleExportOptions(dir, name, Integer.MAX_VALUE, 1, 1, false);
  }
}
