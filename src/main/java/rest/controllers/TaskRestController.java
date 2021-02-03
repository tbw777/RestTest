package rest.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rest.components.TaskService;
import rest.data.Task;

import java.util.Collection;

/**
 * Rest интерфейс операций с задачами
 */
@RestController
@RequestMapping("services/tasks")
public class TaskRestController {

    private static final Logger logger = LoggerFactory.getLogger(TaskRestController.class);

    @Autowired
    private TaskService taskService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody Task task) {
        logger.debug(task.toString());

        if (taskService.create(task)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else { //задача уже существует
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/getById")
    public ResponseEntity<Object> getById(@RequestParam int id) {
        Task taskbyId = taskService.getById(id);

        if (taskbyId != null) {
            return new ResponseEntity<>(taskbyId, HttpStatus.OK);
        } else { //если задача не найдена
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/change")
    public ResponseEntity<Object> change(@RequestBody Task task) {
        Task curTask = taskService.change(task);

        if (curTask != null) {
            return new ResponseEntity<>(curTask, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> delete(@RequestParam int id) {
        if (taskService.delete(id)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else { //не найдена
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Object> list() {
        Collection<Task> list = taskService.list();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
}
