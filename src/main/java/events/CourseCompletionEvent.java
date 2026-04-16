package events;

import com.example.eduskill.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CourseCompletionEvent {
    private final Course course;
}