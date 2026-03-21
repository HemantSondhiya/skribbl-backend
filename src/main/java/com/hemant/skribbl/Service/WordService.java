package com.hemant.skribbl.Service;

import java.util.List;

public interface WordService {
    List<String> randomWords(int count);
    List<String> randomWordsByCategory(String category, int count);
}