package ru.kurochkin.todolistservlets.data;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

public class TodoItemsInMemoryRepository implements TodoItemRepository {
    private static final List<TodoItem> todoItems = new ArrayList<>();
    private static final AtomicInteger newId = new AtomicInteger(1);

    @Override
    public void create(TodoItem item) {
        synchronized (todoItems) {
            item.setId(newId.getAndIncrement());
            todoItems.add(item);
        }
    }

    @Override
    public void update(TodoItem item) {
        synchronized (todoItems) {
            TodoItem repositoryItem = todoItems
                    .stream()
                    .filter(it -> it.getId() == item.getId())
                    .findFirst()
                    .orElse(null);

            if (repositoryItem == null) {
                throw new NoSuchElementException("Запись не найдена по id = " + item.getId());
            }

            repositoryItem.setText(item.getText());
        }
    }

    @Override
    public void delete(int id) {
        synchronized (todoItems) {
            if (!todoItems.removeIf(item -> item.getId() == id)) {
                throw new NoSuchElementException("Запись не найдена по id = " + id);
            }
        }
    }

    public List<TodoItem> getAll() {
        synchronized (todoItems) {
            return todoItems
                    .stream()
                    .map(TodoItem::new)
                    .toList();
        }
    }
}
