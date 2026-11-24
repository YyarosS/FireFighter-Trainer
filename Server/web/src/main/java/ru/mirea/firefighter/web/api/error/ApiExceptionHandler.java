package ru.mirea.firefighter.web.api.error;

import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.mirea.firefighter.application.exception.NotFoundException;
import ru.mirea.firefighter.domain.DomainException;

/**
 * Единая трансляция исключений в HTTP-ответы формата Problem Details (RFC 9457).
 *
 * <p>Контроллеры не содержат try/catch: прикладные и доменные исключения
 * обрабатываются в одном месте, поэтому формат ошибок одинаков для всех
 * эндпоинтов.</p>
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail notFound(NotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Сущность не найдена", exception.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail conflict(DomainException exception) {
        return problem(HttpStatus.CONFLICT, "Нарушено бизнес-правило", exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail badRequest(IllegalArgumentException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Некорректные данные", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException exception) {
        String details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, "Ошибка валидации", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail unreadableBody(HttpMessageNotReadableException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Некорректный JSON",
                "Тело запроса не соответствует ожидаемому формату");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}