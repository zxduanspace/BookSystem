package com.zd.bookmanagementsystem.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zd.bookmanagementsystem.model.Book;
import com.zd.bookmanagementsystem.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static com.zd.bookmanagementsystem.testutil.BookTestData.book1Builder;
import static com.zd.bookmanagementsystem.testutil.BookTestData.book2Builder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BookComponentTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    BookRepository bookRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void should_return_all_books_when_call_get_all_books_api() throws Exception {
        Book book1 = book1Builder.build();
        Book book2 = book2Builder.build();
        bookRepository.saveAll(List.of(book1, book2));

        MvcResult result = mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andReturn();
        List<Book> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertEquals(2, response.size());
        assertThat(response).usingRecursiveComparison().ignoringFields("id").ignoringCollectionOrder().isEqualTo(List.of(book1, book2));
    }

    @Test
    public void should_return_book_when_call_get_book_by_id_api_given_exist_id() throws Exception {
        Book savedBook = book1Builder.build();
        bookRepository.save(savedBook);

        MvcResult result = mvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andReturn();
        Book response = objectMapper.readValue(result.getResponse().getContentAsString(), Book.class);

        assertEquals(savedBook, response);
    }

    @Test
    public void should_return_created_when_call_create_book_api() throws Exception {
        Book bookRequest = book1Builder.build();
        String content = objectMapper.writeValueAsString(bookRequest);

        MvcResult result = mvc.perform(post("/books")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        Book response = objectMapper.readValue(result.getResponse().getContentAsString(), Book.class);

        assertThat(response).usingRecursiveComparison().ignoringFields("id").isEqualTo(bookRequest);
    }

    @Test
    public void should_return_created_when_call_update_book_api_given_exist_id() throws Exception {
        Book book = book1Builder.build();
        Book savedBook = bookRepository.save(book);
        Book bookRequest = Book.builder()
                .id(savedBook.getId())
                .title("new-title")
                .author("new-author")
                .publicationYear("2024")
                .isbn("456-789")
                .build();
        String content = objectMapper.writeValueAsString(bookRequest);

        MvcResult result = mvc.perform(put("/books/" + savedBook.getId())
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        Book response = objectMapper.readValue(result.getResponse().getContentAsString(), Book.class);

        assertEquals(bookRequest, response);
    }

    @Test
    public void should_return_no_content_when_delete_book_given_exist_id() throws Exception {
        Book savedBook = book1Builder.build();
        bookRepository.save(savedBook);

        mvc.perform(delete("/books/1"))
                .andExpect(status().isNoContent())
                .andReturn();

        Optional<Book> optionalBook = bookRepository.findById(1L);
        assertEquals(Optional.empty(), optionalBook);
    }
}
