package com.hemant.skribbl.config;

import com.hemant.skribbl.Repo.WordRepository;
import com.hemant.skribbl.model.Word;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WordDataInitializer implements CommandLineRunner {

    private final WordRepository wordRepository;

    @Override
    public void run(String... args) {
        if (wordRepository.count() > 0) {
            return;
        }

        List<Word> seedWords = List.of(
                word("apple", "easy"),
                word("banana", "easy"),
                word("car", "easy"),
                word("house", "easy"),
                word("river", "easy"),
                word("mountain", "medium"),
                word("guitar", "medium"),
                word("pencil", "easy"),
                word("computer", "medium"),
                word("football", "easy"),
                word("helicopter", "hard"),
                word("volcano", "hard")
        );

        wordRepository.saveAll(seedWords);
    }

    private Word word(String value, String category) {
        Word word = new Word();
        word.setValue(value);
        word.setCategory(category);
        return word;
    }
}
