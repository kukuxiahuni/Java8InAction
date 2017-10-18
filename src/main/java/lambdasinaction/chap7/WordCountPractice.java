package lambdasinaction.chap7;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by kukuxiahuni on 2017/10/18.
 */
public class WordCountPractice {

    public static final String SENTENCE =
            " Nel   mezzo del cammin  di nostra  vita " +
                    "mi  ritrovai in una  selva oscura" +
                    " che la  dritta via era   smarrita ";

    public static void main(String[] args) {
        Stream<Character> stream = IntStream.range(0, SENTENCE.length()).mapToObj(SENTENCE::charAt);

//        stream.forEach(System.out::println);
        WordCountPractice wordCountPractice = new WordCountPractice();
        System.out.println(wordCountPractice.countWords(stream));
        System.out.println(wordCountPractice.parallerCountWords());

    }


    /**
     * @param stream
     * @return
     */
    private int countWords(Stream<Character> stream) {

        WordCounter wordCounter = stream.reduce(new WordCounter(0, true), WordCounter::acc, WordCounter::combine);
        return wordCounter.counter;
    }

    private int parallerCountWords() {
        Spliterator<Character> spliterator = new WordCountSpliterator(SENTENCE);
        Stream<Character> stream = StreamSupport.stream(spliterator, true);

        WordCounter wordCounter = stream.reduce(new WordCounter(0, true), WordCounter::acc, WordCounter::combine);
        return wordCounter.counter;
    }

    /**
     * 计数器内部类
     */
    private final static class WordCounter {

        private final int counter;
        private final boolean lastSpace;

        public WordCounter(int counter, boolean lastSpace) {
            this.counter = counter;
            this.lastSpace = lastSpace;
        }

        /**
         * 累加单词数
         *
         * @param character
         * @return
         */
        public WordCounter acc(Character character) {
            if (Character.isWhitespace(character)) {
                return lastSpace ? this : new WordCounter(counter, true);
            } else {
                return lastSpace ? new WordCounter(counter + 1, false) : this;
            }
        }

        /**
         * 合并两个count
         *
         * @param wordCounter
         * @return
         */
        public WordCounter combine(WordCounter wordCounter) {
            return new WordCounter(this.counter + wordCounter.counter, wordCounter.lastSpace);
        }

    }

    private final static class WordCountSpliterator implements Spliterator<Character> {
        private final String string;
        private int currentChar = 0;

        public WordCountSpliterator(String string) {
            this.string = string;
        }


        @Override
        public boolean tryAdvance(Consumer<? super Character> action) {

            action.accept(this.string.charAt(currentChar++));
            return this.currentChar < string.length();
        }

        @Override
        public Spliterator<Character> trySplit() {

            int currentSize = string.length() - currentChar;
            if (currentSize < 10) {
                return null;
            }

            for (int splitPos = currentSize / 2 + this.currentChar; splitPos < string.length(); ++splitPos) {
                if (Character.isWhitespace(string.charAt(splitPos))) {
                    Spliterator<Character> spliterator = new WordCountSpliterator(string.substring(currentChar, splitPos));
                    this.currentChar = splitPos;
                    return spliterator;
                }
            }
            return null;
        }

        @Override
        public long estimateSize() {
            return string.length() - currentChar;
        }

        @Override
        public int characteristics() {
            return ORDERED + SIZED + SUBSIZED + NONNULL + IMMUTABLE;
        }

    }

}
