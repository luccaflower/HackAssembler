package io.github.luccaflower.hack;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

@FunctionalInterface
public interface Lexer<T> {
    default T parse(CharSequence in) throws ParseException {
        return tryParse(in).parsed();
    }

    Parsed<T> tryParse(CharSequence in) throws ParseException;

    static Lexer<String> regex(Pattern regex) {
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

    static Lexer<String> regex(String pattern) {
        return regex(Pattern.compile(pattern));
    }

    static Lexer<String> string(String s) {
        return regex(Pattern.compile(Pattern.quote(s)));
    }

    static Lexer<String> eol() {
        return string("\n").or(eof());
    }

    static Lexer<String> eof() {
        return in -> {
            if (!in.isEmpty()) {
                throw new ParseException("expected EOF");
            } else {
                return new Parsed<>("", "");
            }
        };
    }

    default <U> Lexer<U> map(Function<? super T, ? extends U> f) {
        return in -> {
            var parsed = tryParse(in);
            return new Parsed<>(f.apply(parsed.parsed()), parsed.rest());
        };
    }

    default <U> Lexer<Pair<T, U>> andThen(Lexer<U> other) {
        return in -> {
            var first = tryParse(in);
            var second = other.tryParse(first.rest());
            return new Parsed<>(new Pair<>(first.parsed(), second.parsed()), second.rest());
        };
    }

    default Lexer<T> or(Lexer<T> other) {
        return in -> {
            try {
                return tryParse(in);
            } catch (ParseException ignored) {
                return other.tryParse(in);
            }
        };
    }

    default Lexer<Queue<T>> repeating() {
        return in -> repeating(new ArrayDeque<>()).tryParse(in);
    }

    default Lexer<T> andSkip(Lexer<?> other) {
        return andThen(other).map(Pair::left);
    }

    default <U> Lexer<U> skipAnd(Lexer<U> other) {
        return andThen(other).map(Pair::right);
    }

    private Lexer<Queue<T>> repeating(Queue<T> parsed) {
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