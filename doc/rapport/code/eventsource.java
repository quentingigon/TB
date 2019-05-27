@Singleton
public class EventSourceController extends Controller implements Observer {

    private static Source<String, ?> source;

    @Override
    public synchronized void update(Observable o, Object arg) {

        if (source == null) {
            source = Source.tick(Duration.ZERO, Duration.ofSeconds(5), "tick");
        }
        else {
            List<String> list = new ArrayList<>();
            list.add((String) arg);
            Source<String, ?> s = Source.from(list);
            source.merge(s);
        }
    }

    public Result events() {

        final Source<EventSource.Event, ?> eventSource;

        return ok().chunked(source
            .map(EventSource.Event::event)
            .via(EventSource.flow()))
            .as(Http.MimeTypes.EVENT_STREAM);

    }
}