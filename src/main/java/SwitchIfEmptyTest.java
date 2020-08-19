/*
 * Copyright (c) 2016-2020, Leftshift One
 * __________________
 * [2020] Leftshift One
 * All Rights Reserved.
 * NOTICE:  All information contained herein is, and remains
 * the property of Leftshift One and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Leftshift One
 * and its suppliers and may be covered by Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Leftshift One.
 */

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
                                .toList()
                                .toFlowable()
                                .flatMap(strings -> {
                                    Flowable<String> flowable = Flowable.empty();
                                    for (String s : strings) {
                                        flowable = flowable.switchIfEmpty(third(s));
                                    }
                                    return flowable;
                                });
                    });
        }

        public Flowable<String> third(String input) {
            //System.out.println("Value " + input);
            return Flowable.empty();
        }
    }

    static class SomeSource {

        public Flowable<String> read() {
            return Flowable.create(emitter -> {
                for (int i = 0; i < 1_000_000; i++) {
                    emitter.onNext("Some values " + i);
                }
                emitter.onComplete();
            }, BackpressureStrategy.ERROR);
        }
    }
}
