package com.example.wordlearning.repository;

import com.example.wordlearning.dataobject.WordDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WordRepository extends JpaRepository<WordDO, Long> {
    Optional<WordDO> findFirstByEnglishIgnoreCase(String english);

    List<WordDO> findByEnglishIgnoreCaseContaining(String english);

    List<WordDO> findByEnglishIgnoreCaseContaining(String english, Pageable pageable);

    List<WordDO> findByChineseContaining(String chinese);

    List<WordDO> findByChineseContaining(String chinese, Pageable pageable);

    List<WordDO> findByCategoryOrderByEnglishAsc(String category);

    List<WordDO> findByCategoryOrderByEnglishAsc(String category, Pageable pageable);

    List<WordDO> findAllByOrderByEnglishAsc(Pageable pageable);

    @Query("select distinct w.category from WordDO w where w.category is not null and w.category <> '' order by w.category")
    List<String> findCategories();

    @Query("select w.category, count(w) from WordDO w group by w.category order by count(w) desc")
    List<Object[]> countByCategory();

    @Query(value = "select * from sekai_word_words order by rand() limit 1", nativeQuery = true)
    WordDO findRandomOne();

    @Query(value = "select * from sekai_word_words order by rand() limit ?1", nativeQuery = true)
    List<WordDO> findRandomList(int limit);

    @Query(value = "select * from sekai_word_words where category = ?1 order by rand() limit 1", nativeQuery = true)
    WordDO findRandomOneByCategory(String category);

    @Query(value = "select * from sekai_word_words where category = ?1 order by rand() limit ?2", nativeQuery = true)
    List<WordDO> findRandomListByCategory(String category, int limit);
}

