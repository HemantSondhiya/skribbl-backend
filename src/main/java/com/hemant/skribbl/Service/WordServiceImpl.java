package com.hemant.skribbl.Service;


import com.hemant.skribbl.Repo.WordRepository;
import com.hemant.skribbl.model.Word;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class WordServiceImpl implements WordService {

    private final WordRepository wordRepository;
    private final SecureRandom random = new SecureRandom();
    private static final List<String> DEFAULT_WORDS = Arrays.asList(
            "apple", "banana", "car", "house", "river",
            "mountain", "guitar", "pencil", "computer", "football"
    );

    @Override
    public List<String> randomWords(int count) {
        return getRandomWordsFromList(wordRepository.findAll(), count);
    }

    @Override
    public List<String> randomWordsByCategory(String category, int count) {
        List<Word> words = wordRepository.findByCategoryIgnoreCase(category);

        if (words.isEmpty()) {
            throw new IllegalStateException("No words found for category: " + category);
        }

        return getRandomWordsFromList(words, count);
    }

    private List<String> getRandomWordsFromList(List<Word> words, int count) {
        if (words.isEmpty()) {
            return pickFromDefaults(count);
        }

        List<Word> shuffled = new ArrayList<>(words);
        Collections.shuffle(shuffled, random);

        return shuffled.stream()
                .limit(Math.max(1, count))
                .map(Word::getValue)
                .collect(Collectors.toList());
    }

    private List<String> pickFromDefaults(int count) {
        List<String> shuffled = new ArrayList<>(DEFAULT_WORDS);
        Collections.shuffle(shuffled, random);
        return shuffled.stream()
                .limit(Math.max(1, count))
                .collect(Collectors.toList());
    }
}
