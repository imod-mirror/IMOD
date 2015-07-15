package etomo.type;

public final class StatusChangeEvent<S> {
  private final S status;

 public StatusChangeEvent(final S status) {
    this.status = status;
  }
}
