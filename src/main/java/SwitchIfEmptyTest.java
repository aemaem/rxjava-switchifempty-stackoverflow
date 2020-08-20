import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

/**
 * @author Michael Mair
 */
public class SwitchIfEmptyTest {

    public static void main(String[] args) {
        SwitchIfEmptyDemo demo = new SwitchIfEmptyDemo();
        demo.one("foo")
                .blockingForEach(s -> System.out.println(s));
    }

    static class SwitchIfEmptyDemo {

        private SomeSource source = new SomeSource();

        public Flowable<String> one(String input) {
            return Flowable.<String>empty()
                    .switchIfEmpty(two(input));
        }

        public Flowable<String> two(String input) {
            return Flowable.<String>create(emitter -> {
                emitter.onNext(input);
                emitter.onComplete();
            }, BackpressureStrategy.ERROR)
                    .flatMap(inputFlowable -> {
                        return source.read()
                                .flatMap(this::third)
                                .takeWhile(s -> s.equals("false"));
                    });
        }

        public Flowable<String> third(String input) {
            //System.out.println("Value " + input);
            return Flowable.just("false");
        }
    }

    static class SomeSource {

        public Flowable<String> read() {
            return Flowable.create(emitter -> {
                for (int i = 0; i < 1_000_000; i++) {
                    emitter.onNext("Some values " + i);
                }
                emitter.onComplete();
            }, BackpressureStrategy.BUFFER);
        }
    }
}
