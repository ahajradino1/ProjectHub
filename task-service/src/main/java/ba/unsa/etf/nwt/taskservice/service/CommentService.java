package ba.unsa.etf.nwt.taskservice.service;

import ba.unsa.etf.nwt.taskservice.client.dto.UserDTO;
import ba.unsa.etf.nwt.taskservice.dto.CommentDTO;
import ba.unsa.etf.nwt.taskservice.exception.base.NotFoundException;
import ba.unsa.etf.nwt.taskservice.model.Comment;
import ba.unsa.etf.nwt.taskservice.model.Task;
import ba.unsa.etf.nwt.taskservice.repository.CommentRepository;
import ba.unsa.etf.nwt.taskservice.request.create.CreateCommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    public Comment create(final CreateCommentRequest request, final Task task, final UserDTO author) {
        Comment comment = createCommentFromRequest(request, task, author);
        return commentRepository.save(comment);
    }

    private Comment createCommentFromRequest(final CreateCommentRequest request, final Task task, final UserDTO author) {
        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setUserId(author.getId());
        comment.setUserFirstName(author.getFirstName());
        comment.setUserLastName(author.getLastName());
        comment.setTask(task);
        return comment;
    }

    public Comment findById(final UUID commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> {
            throw new NotFoundException("Comment not found");
        });
    }

    public void delete(final Comment comment) {
        commentRepository.delete(comment);
    }

    public Page<CommentDTO> getCommentsForTask(final Task task, final Pageable pageable) {
        return commentRepository.findAllByTaskOrderByCreatedAtDesc(task, pageable);
    }

    public Comment findByIdAndTaskId(final UUID commentId, final UUID taskId) {
        return commentRepository.findByIdAndTask_Id(commentId, taskId).orElseThrow(() -> {
            throw new NotFoundException("Comment not found");
        });
    }

    public Comment patch(final Comment comment, final String text) {
        comment.setText(text);
        return commentRepository.save(comment);
    }
}
