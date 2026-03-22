package com.hemant.skribbl.config;

import com.hemant.skribbl.Repo.WordRepository;
import com.hemant.skribbl.model.Word;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WordDataInitializer implements CommandLineRunner {

    private final WordRepository wordRepository;

    @Override
    public void run(String... args) {
        List<Word> seedWords = List.of(
                word("apple", "easy"),
                word("banana", "easy"),
                word("car", "easy"),
                word("house", "easy"),
                word("tree", "easy"),
                word("dog", "easy"),
                word("cat", "easy"),
                word("sun", "easy"),
                word("moon", "easy"),
                word("star", "easy"),
                word("book", "easy"),
                word("chair", "easy"),
                word("table", "easy"),
                word("clock", "easy"),
                word("phone", "easy"),
                word("shoe", "easy"),
                word("fish", "easy"),
                word("bird", "easy"),
                word("flower", "easy"),
                word("river", "easy"),
                word("beach", "easy"),
                word("cloud", "easy"),
                word("bread", "easy"),
                word("milk", "easy"),
                word("cheese", "easy"),
                word("egg", "easy"),
                word("train", "easy"),
                word("bus", "easy"),
                word("boat", "easy"),
                word("plane", "easy"),
                word("drum", "easy"),
                word("ball", "easy"),
                word("candle", "easy"),
                word("bridge", "easy"),
                word("mountain", "medium"),
                word("guitar", "medium"),
                word("pencil", "medium"),
                word("computer", "medium"),
                word("football", "medium"),
                word("camera", "medium"),
                word("bicycle", "medium"),
                word("laptop", "medium"),
                word("library", "medium"),
                word("airport", "medium"),
                word("hospital", "medium"),
                word("teacher", "medium"),
                word("student", "medium"),
                word("garden", "medium"),
                word("desert", "medium"),
                word("forest", "medium"),
                word("island", "medium"),
                word("turtle", "medium"),
                word("dolphin", "medium"),
                word("pirate", "medium"),
                word("castle", "medium"),
                word("rocket", "medium"),
                word("planet", "medium"),
                word("diamond", "medium"),
                word("monster", "medium"),
                word("rainbow", "medium"),
                word("blanket", "medium"),
                word("picture", "medium"),
                word("popcorn", "medium"),
                word("sandwich", "medium"),
                word("necklace", "medium"),
                word("backpack", "medium"),
                word("waterfall", "medium"),
                word("helicopter", "hard"),
                word("volcano", "hard"),
                word("microscope", "hard"),
                word("submarine", "hard"),
                word("astronaut", "hard"),
                word("labyrinth", "hard"),
                word("chameleon", "hard"),
                word("skyscraper", "hard"),
                word("parliament", "hard"),
                word("lightning", "hard"),
                word("pterodactyl", "hard"),
                word("metamorphosis", "hard"),
                word("photosynthesis", "hard"),
                word("archaeologist", "hard"),
                word("thermometer", "hard"),
                word("hippopotamus", "hard"),
                word("constellation", "hard"),
                word("jukebox", "hard"),
                word("kaleidoscope", "hard"),
                word("windmill", "hard"),
                word("catapult", "hard"),
                word("sarcophagus", "hard"),
                word("quicksand", "hard"),
                word("rhinoceros", "hard"),
                word("saxophone", "hard"),
                word("timekeeper", "hard"),
                word("voyager", "hard"),
                word("zeppelin", "hard"),
                word("blacksmith", "hard"),
                word("crossbow", "hard"),
                word("aftershock", "hard"),
                word("blueprint", "hard"),
                word("cyberspace", "hard")
        );

        Set<String> existingWords = new HashSet<>();
        wordRepository.findAll().forEach(existing -> existingWords.add(normalize(existing.getValue())));

        List<Word> missingWords = seedWords.stream()
                .filter(seed -> !existingWords.contains(normalize(seed.getValue())))
                .toList();

        if (!missingWords.isEmpty()) {
            wordRepository.saveAll(missingWords);
        }
    }

    private Word word(String value, String category) {
        Word word = new Word();
        word.setValue(value);
        word.setCategory(category);
        return word;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
