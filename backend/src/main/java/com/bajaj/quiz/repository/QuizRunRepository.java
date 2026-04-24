package com.bajaj.quiz.repository;

import com.bajaj.quiz.entity.QuizRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuizRunRepository extends JpaRepository<QuizRun, UUID> {
}
