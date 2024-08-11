package ru.kurochkin.todolistservlets.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.text.StringEscapeUtils;
import ru.kurochkin.todolistservlets.data.TodoItem;
import ru.kurochkin.todolistservlets.data.TodoItemRepository;
import ru.kurochkin.todolistservlets.data.TodoItemsInMemoryRepository;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.util.*;

@WebServlet("")
public class TodoListServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");

        PrintWriter writer = resp.getWriter();

        String baseUrl = getServletContext().getContextPath() + "/";

        HttpSession session = req.getSession(false);

        String actionErrorMessageHtml = "";
        String createErrorMessageHtml = "";
        String itemErrorMessageHtml = "";
        int editingItemId = -1;

        if (session != null) {
            String errorMessage = (String) session.getAttribute("actionErrorText");

            if (errorMessage != null) {
                actionErrorMessageHtml = "<hr><div>%s</div><hr>".formatted(StringEscapeUtils.escapeHtml4(errorMessage));
                session.removeAttribute("actionErrorText");
            }

            String createErrorMessage = (String) session.getAttribute("createErrorText");

            if (createErrorMessage != null) {
                createErrorMessageHtml = "<div>%s</div>".formatted(StringEscapeUtils.escapeHtml4(createErrorMessage));
                session.removeAttribute("createErrorText");
            }

            Integer sessionEditingItemId = (Integer) session.getAttribute("editingItemId");

            if (sessionEditingItemId != null) {
                editingItemId = sessionEditingItemId;

                session.removeAttribute("editingItemId");
            }

            String editingItemErrorText = (String) session.getAttribute("editingItemErrorText");

            if (editingItemErrorText != null) {
                itemErrorMessageHtml = "<div>%s</div>".formatted(StringEscapeUtils.escapeHtml4(editingItemErrorText));

                session.removeAttribute("editingItemErrorText");
            }
        }

        TodoItemRepository todoItemRepository = new TodoItemsInMemoryRepository();
        List<TodoItem> todoItems = todoItemRepository.getAll();

        StringBuilder todoListHtml = new StringBuilder();

        for (TodoItem todoItem : todoItems) {
            int itemId = todoItem.getId();

            if (itemId == editingItemId) {
                String itemText = itemErrorMessageHtml.isEmpty() ? todoItem.getText() : "";

                todoListHtml
                        .append("""
                                <li>
                                    <form action="%s" method="POST">
                                        <input type="text" name="text" value="%s">
                                        <button name="action" value="cancel" type="submit">Отменить</button>
                                        <button name="action" value="save" type="submit">Сохранить</button>
                                        <input type="hidden" name="id" value="%s">
                                        %s
                                    </form>
                                </li>
                                """.formatted(baseUrl,
                                StringEscapeUtils.escapeHtml4(itemText),
                                itemId,
                                itemErrorMessageHtml))
                        .append(System.lineSeparator());
            } else {
                todoListHtml
                        .append("""
                                <li>
                                    <form action="%s" method="POST">
                                        <span>%s</span>
                                        <button name="action" value="delete" type="submit">Удалить</button>
                                        <button name="action" value="edit" type="submit">Редактировать</button>
                                        <input type="hidden" name="id" value="%s">
                                    </form>
                                </li>
                                """.formatted(baseUrl,
                                StringEscapeUtils.escapeHtml4(todoItem.getText()),
                                todoItem.getId()))
                        .append(System.lineSeparator());
            }
        }

        writer.println("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>TODO List Servlets</title>
                    <meta charset="UTF-8">
                </head>
                <body>
                    <h1>TODO List Servlets</h1>
                    %s
                    <form action="%s" method="POST">
                        <input name="text" type="text">
                        <button name="action" value="create" type="submit">Создать</button>
                        %s
                    </form>
                    <ul>
                    %s
                    </ul>
                </body>
                </html>
                """.formatted(actionErrorMessageHtml, baseUrl, createErrorMessageHtml, todoListHtml));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");

        switch (action) {
            case "create" -> {
                String text = req.getParameter("text");
                HttpSession session = req.getSession();

                if (text == null) {
                    session.setAttribute("actionErrorText", "Ошибка при добавлении новой записи: не передан обязательный параметр id записи");
                } else {
                    text = text.trim();

                    if (text.isEmpty()) {
                        session.setAttribute("createErrorText", "Необходимо указать текст");
                    } else {
                        TodoItemRepository todoItemRepository = new TodoItemsInMemoryRepository();
                        todoItemRepository.create(new TodoItem(text));
                    }
                }
            }
            case "edit" -> {
                String idString = req.getParameter("id");
                HttpSession session = req.getSession();

                if (idString == null) {
                    session.setAttribute("actionErrorText", "Ошибка при редактировании записи: не передан обязательный параметр id записи");
                } else {
                    try {
                        int id = Integer.parseInt(idString);
                        session.setAttribute("editingItemId", id);
                    } catch (NumberFormatException e) {
                        session.setAttribute("actionErrorText", "Ошибка при редактировании записи: не распознан параметр id записи");
                    }
                }
            }
            case "cancel" -> {
                HttpSession session = req.getSession();

                session.removeAttribute("editingItemId");
            }
            case "save" -> {
                String text = req.getParameter("text");
                String idString = req.getParameter("id");
                HttpSession session = req.getSession();

                if (text == null) {
                    session.setAttribute("actionErrorText", "Ошибка при сохранении записи: не передан обязательный параметр text записи");
                } else if (idString == null) {
                    session.setAttribute("actionErrorText", "Ошибка при сохранении записи: не передан обязательный параметр id записи");
                } else {
                    try {
                        int id = Integer.parseInt(idString);
                        text = text.trim();

                        if (text.isEmpty()) {
                            session.setAttribute("editingItemErrorText", "Необходимо указать текст");
                            session.setAttribute("editingItemId", id);
                        } else {
                            TodoItemRepository todoItemRepository = new TodoItemsInMemoryRepository();
                            todoItemRepository.update(new TodoItem(id, text));
                            session.removeAttribute("editingItemId");
                        }
                    } catch (NumberFormatException e) {
                        session.setAttribute("actionErrorText", "Ошибка при сохранении записи: не распознан параметр id записи");
                    } catch (NoSuchElementException e) {
                        session.setAttribute("actionErrorText", "Ошибка при сохранении записи: " + e.getMessage());
                    }
                }
            }
            case "delete" -> {
                String idString = req.getParameter("id");

                if (idString == null) {
                    HttpSession session = req.getSession();
                    session.setAttribute("actionErrorText", "Ошибка при удалении записи: не передан обязательный параметр id записи");
                } else {
                    try {
                        int id = Integer.parseInt(idString);

                        TodoItemRepository todoItemRepository = new TodoItemsInMemoryRepository();
                        todoItemRepository.delete(id);
                    } catch (NumberFormatException e) {
                        HttpSession session = req.getSession();
                        session.setAttribute("actionErrorText", "Ошибка при удалении записи: не распознан параметр id записи");
                    } catch (NoSuchElementException e) {
                        HttpSession session = req.getSession();
                        session.setAttribute("actionErrorText", "Ошибка при удалении записи: " + e.getMessage());
                    }
                }
            }
            default -> {
                HttpSession session = req.getSession();

                session.setAttribute("actionErrorText", "Системе не удалось определить действие \"%s\"".formatted(action));
            }
        }

        String baseUrl = getServletContext().getContextPath() + "/";
        resp.sendRedirect(baseUrl);
    }
}
