package ru.netology.patterns.test;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;
import ru.netology.patterns.data.DataGenerator;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class CardDeliveryTest {

    @BeforeEach
    void setup() {
        open("http://localhost:9999");
    }

    @Test
    @DisplayName("Should successful plan and replan meeting")
    void shouldSuccessfulPlanAndReplanMeeting() {
        // Генерируем тестовые данные
        var validUser = DataGenerator.generateUser("ru");
        var daysToAddForFirstMeeting = 4;
        var firstMeetingDate = DataGenerator.generateDate(daysToAddForFirstMeeting);
        var daysToAddForSecondMeeting = 7;
        var secondMeetingDate = DataGenerator.generateDate(daysToAddForSecondMeeting);

        // Заполняем форму первый раз
        $("[data-test-id=city] input").setValue(validUser.getCity());

        // Очищаем поле даты и вводим новую дату
        $("[data-test-id=date] input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        $("[data-test-id=date] input").setValue(firstMeetingDate);

        $("[data-test-id=name] input").setValue(validUser.getName());
        $("[data-test-id=phone] input").setValue(validUser.getPhone());

        // Ставим галочку согласия
        $("[data-test-id=agreement]").click();

        // Нажимаем кнопку "Запланировать"
        $(Selectors.byText("Запланировать")).click();

        // Проверяем первое уведомление об успехе
        $(Selectors.withText("Успешно"))
                .shouldBe(Condition.visible, Duration.ofSeconds(15));

        $("[data-test-id=success-notification] .notification__content")
                .shouldHave(exactText("Встреча успешно запланирована на " + firstMeetingDate))
                .shouldBe(visible);

        // Меняем дату и пробуем запланировать снова
        $("[data-test-id=date] input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        $("[data-test-id=date] input").setValue(secondMeetingDate);
        $(Selectors.byText("Запланировать")).click();

        // Проверяем уведомление о перепланировании
        $("[data-test-id=replan-notification] .notification__content")
                .shouldHave(text("У вас уже запланирована встреча на другую дату. Перепланировать?"))
                .shouldBe(visible);

        // Нажимаем кнопку перепланировать
        $("[data-test-id=replan-notification] button").click();

        // Проверяем финальное уведомление об успехе
        $("[data-test-id=success-notification] .notification__content")
                .shouldHave(exactText("Встреча успешно запланирована на " + secondMeetingDate))
                .shouldBe(visible);
    }

    @Test
    @DisplayName("Should show error if date is invalid")
    void shouldShowErrorIfDateIsInvalid() {
        // Генерируем тестовые данные с невалидной датой (завтра)
        var invalidUser = DataGenerator.generateUser("ru");

        // Заполняем форму с датой, которая слишком близко (завтра)
        $("[data-test-id=city] input").setValue(invalidUser.getCity());

        // Очищаем поле даты
        $("[data-test-id=date] input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);

        // Вводим невалидную дату (завтра)
        $("[data-test-id=date] input").setValue(DataGenerator.generateDate(1));

        $("[data-test-id=name] input").setValue(invalidUser.getName());
        $("[data-test-id=phone] input").setValue(invalidUser.getPhone());

        // Ставим галочку согласия
        $("[data-test-id=agreement]").click();

        // Нажимаем кнопку "Запланировать"
        $(Selectors.byText("Запланировать")).click();

        // Проверяем сообщение об ошибке
        $("[data-test-id=date] .input__sub")
                .shouldHave(text("Заказ на выбранную дату невозможен"));
    }
}