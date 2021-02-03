package rest.controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.AssertionErrors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import rest.data.Task;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskRestControllerTest {

    @Autowired
    private MockMvc mvc;

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    public void createPos1() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/services/tasks/create")
                .content("{\"id\":1,\"name\":\"First\",\"description\":null,\"lastModifiedDate\":null}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void createNeg1() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/services/tasks/create")
                .content("{\"id\":2,\"name\":\"First\",\"description\":null,\"lastModifiedDate\":null}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mvc.perform(MockMvcRequestBuilders.post("/services/tasks/create")
                .content("{\"id\":2,\"name\":\"First\",\"description\":null,\"lastModifiedDate\":null}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isConflict()); //id 2 = 2
    }

    @Test
    public void getByIdPos1() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/services/tasks/create")
                .content("{\"id\":3,\"name\":\"First\",\"description\":null,\"lastModifiedDate\":null}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mvc.perform(MockMvcRequestBuilders.get("/services/tasks/getById")
                .param("id", "3")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void getByIdNeg1() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/services/tasks/getById")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void deletePos1() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/services/tasks/create")
                .content("{\"id\":4,\"name\":\"First\",\"description\":null,\"lastModifiedDate\":null}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mvc.perform(MockMvcRequestBuilders.delete("/services/tasks/delete")
                .param("id", "4")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void deleteNeg1() throws Exception { //несуществующий индекс
        mvc.perform(MockMvcRequestBuilders.delete("/services/tasks/delete")
                .param("id", "5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void listPos1() throws Exception {
        Set<Integer> taskSet = new HashSet<>(100);

        for (int i = 6; i < 100; i++) {
            String body = "{\"id\":" + i + ",\"name\":\"First\",\"description\":null,\"lastModifiedDate\":null}";

            taskSet.add(i);

            mvc.perform(MockMvcRequestBuilders.post("/services/tasks/create")
                    .content(body)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get("/services/tasks/list")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Task[] tasks = parseResponse(mvcResult, Task[].class);


        //не строгое равенство если успели выполнится другие тесты раньше текущего
        AssertionErrors.assertTrue("invalid list size", tasks.length >= 100 - 6);

        for (Task task : tasks) {
            taskSet.remove(task.getId());
        }

        AssertionErrors.assertTrue("tasks invalid state detected", taskSet.isEmpty());
    }

    public static <T> T parseResponse(MvcResult result, Class<T> responseClass) {
        try {
            String contentAsString = result.getResponse().getContentAsString();
            return MAPPER.readValue(contentAsString, responseClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void changePos1() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/services/tasks/create")
                .content("{\"id\":101,\"name\":\"First\",\"description\":null,\"lastModifiedDate\":null}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.put("/services/tasks/change")
                .content("{\"id\":101,\"name\":\"Second\",\"description\":null,\"lastModifiedDate\":null}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Task task = parseResponse(mvcResult, Task.class);

        AssertionErrors.assertTrue("invalid task 'name' field value", task.getName().equals("Second"));
    }

    @Test
    public void changeNeg1() throws Exception { //несуществующий индекс
        mvc.perform(MockMvcRequestBuilders.put("/services/tasks/change")
                .content("{\"id\":102,\"name\":\"Second\",\"description\":null,\"lastModifiedDate\":null}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }
}
