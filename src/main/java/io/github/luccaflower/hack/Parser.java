package io.github.luccaflower.hack;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

@FunctionalInterface
public interface Parser<T> {
    default T parse(CharSequence in) throws ParseException {
        return tryParse(in).parsed();
    }

    Parsed<T> tryParse(CharSequence in) throws ParseException;

    static Parser<String> regex(Pattern regex) {
        return in -> {
            var matcher = regex.matcher(in);
            if (matcher.lookingAt()) {
                var end = matcher.end();
                var parsed = matcher.group();
                return new Parsed<>(parsed, in.subSequence(end, in.length()));
            } else {
                throw new ParseException("No match for input");
            }
        };
    }

    static Parser<String> regex(String pattern) {
        return regex(Pattern.compile(pattern));
    }

    static Parser<String> string(String s) {
        return regex(Pattern.compile(Pattern.quote(s)));
    }

    static Parser<String> eol() {
        return string("\n");
    }

    static Parser<String> eof() {
        return in -> {
            if (!in.isEmpty()) {
                throw new ParseException("expected EOF");
            } else {
                return new Parsed<>("", "");
            }
        };
    }

    default <U> Parser<U> map(Function<? super T, ? extends U> f) {
        return in -> {
            var parsed = tryParse(in);
            return new Parsed<>(f.apply(parsed.parsed()), parsed.rest());
        };
    }

    default <U> Parser<Pair<T, U>> andThen(Parser<U> other) {
        return in -> {
            var first = tryParse(in);
            var second = other.tryParse(first.rest());
            return new Parsed<>(new Pair<>(first.parsed(), second.parsed()), second.rest());
        };
    }

    default Parser<T> or(Parser<T> other) {
        return in -> {
            try {
                return tryParse(in);
            } catch (ParseException ignored) {
                return other.tryParse(in);
            }
        };
    }

    default Parser<Queue<T>> repeating() {
        return in -> repeating(new ArrayDeque<>()).tryParse(in);
    }

    default Parser<T> andSkip(Parser<?> other) {
        return andThen(other).map(Pair::left);
    }

    default <U> Parser<U> skipAnd(Parser<U> other) {
        return andThen(other).map(Pair::right);
    }

    private Parser<Queue<T>> repeating(Queue<T> parsed) {
        return in -> {
            try {
                var result = tryParse(in);
                var queue = new ArrayDeque<>(parsed);
                queue.add(result.parsed());
                return repeating(queue).tryParse(result.rest());
            } catch (ParseException ignored) {
                return new Parsed<>(parsed, in);
            }
        };
    }

    record Parsed<T>(T parsed, CharSequence rest) {}

    class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }

    record Pair<T, U>(T left, U right) {
        public <R> R reduce(BiFunction<T, U, R> f) {
            return f.apply(left, right);
        }
    }
}
