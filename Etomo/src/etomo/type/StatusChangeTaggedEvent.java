package etomo.type;

public class StatusChangeTaggedEvent implements StatusChangeEvent {
  private final String tag;
  private final String string;
  private final Status status;

  public StatusChangeTaggedEvent(final String tag, final Status status) {
    this.tag = tag;
    string = null;
    this.status = status;
  }

  public StatusChangeTaggedEvent(final String tag, final String string,
    final Status status) {
    this.tag = tag;
    this.string = string;
    this.status = status;
  }

  public boolean equals(final String input) {
    return (tag == null && input == null) || (tag != null && tag.equals(input));
  }

  public String getString() {
    return string;
  }

  public Status getStatus() {
    return status;
  }
}
