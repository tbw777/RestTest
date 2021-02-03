package rest.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import rest.controllers.TaskRestController;
import rest.data.Task;

import java.time.LocalDate;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис реализует операции над задачами
 */
@Component
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private static final ConcurrentHashMap<Integer, Task> TASKS_MAP = new ConcurrentHashMap<>();

    /**
     * Создать задачу
     * @param task данные задачи
     * @return true если задача была создана, false если уже существует
     */
    public boolean create(Task task) {
        int id = task.getId();
        logger.debug("creating task with id {}", id);

        return TASKS_MAP.putIfAbsent(id, task) == null;
    }

    /**
     * Получить задачу по идентификатору
     * @param id идентификатор задачи
     * @return данные задачи или null если задача не найдена
     */
    public Task getById(int id) {
        logger.debug("getting task with id {}", id);
        return TASKS_MAP.get(id);
    }

    /**
     * Изменить задачу
     * @param newTask данные задачи для актуализации
     * @return актуальное состояние задачи или null если задача не найдена
     */
    public Task change(Task newTask) {
        int id = newTask.getId();

        logger.debug("changing task with id {}", id);

        if (!TASKS_MAP.containsKey(id)) {
            return null;
        }

        //"All changes to tasks must occur atomically."
        return TASKS_MAP.merge(id, newTask, (oldVal, newVal) -> {
            //"You cannot delete task attributes."

            if (newVal.getName() == null || newVal.getName().isEmpty()) {
                String curName = oldVal.getName();
                if (curName != null && !curName.isEmpty()) {
                    newVal.setName(curName);
                }
            }

            if (newVal.getDescription() == null || newVal.getDescription().isEmpty()) {
                String curDesc = oldVal.getDescription();
                if (curDesc != null && !curDesc.isEmpty()) {
                    newVal.setDescription(curDesc);
                }
            }

            if (newVal.getLastModifiedDate() == null) {
                LocalDate curLstModified = oldVal.getLastModifiedDate();
                if (curLstModified != null) {
                    newVal.setLastModifiedDate(curLstModified);
                }
            }
            
            return newVal;
        });
    }

    /**
     * Удалить задачу по идентификатору
     * @param id идентификатор задачи
     * @return true если задача была удалена
     */
    public boolean delete(int id) {
        logger.debug("deleting task with id {}", id);
        return TASKS_MAP.remove(id) != null;
    }

    /**
     * Получить перечень всех задач
     * @return перечень задач
     */
    public Collection<Task> list() {
        logger.debug("getting list of tasks");
        return TASKS_MAP.values();
    }
}
