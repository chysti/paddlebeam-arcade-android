package com.chystialex.paddlebeamarcade;

import android.content.Context;
import android.view.View;
import java.util.Locale;

final class I18n {
    private static final String[] WIN_EN={"Great job! Keep it up!","That was brilliant!","You are on fire!","The bricks fear you!","Fantastic! Keep moving!","Another level conquered!","You are a true champion!","What an arcade legend!"};
    private static final String[] WIN_RU={"Молодец! Так держать!","Вот это мастерство!","Ну ты и крутой перец!","Блоки тебя боятся!","Фантастика! Не сбавляй темп!","Ещё один уровень покорён!","Ты настоящий чемпион!","Вот это легенда аркады!"};
    private static final String[] WIN_HE={"כל הכבוד! המשך כך!","איזו שליטה מדהימה!","אתה פשוט לוהט!","הלבנים מפחדות ממך!","נפלא! ממשיכים קדימה!","עוד שלב נכבש!","אתה אלוף אמיתי!","אגדת ארקייד אמיתית!"};
    private static final String[] LOSE_EN={"No worries! Next time will be even better.","Good try! Victory is getting closer.","Every miss teaches something. Try again!","Do not give up — the next game is yours!"};
    private static final String[] LOSE_RU={"Ничего, не переживай! В следующий раз получится лучше.","Хорошая попытка! Победа уже ближе.","Каждый промах чему-то учит. Попробуем ещё раз!","Не сдавайся — следующая партия будет твоей!"};
    private static final String[] LOSE_HE={"לא נורא! בפעם הבאה יהיה אפילו טוב יותר.","ניסיון טוב! הניצחון כבר קרוב.","מכל פספוס לומדים. ננסה שוב!","לא מוותרים — המשחק הבא שלך!"};
    static String language(Context c){
        String saved=selection(c);
        if(!"system".equals(saved))return saved;
        String phone=Locale.getDefault().getLanguage();
        if("he".equals(phone)||"iw".equals(phone))return "he";
        if("ru".equals(phone))return "ru";
        return "en";
    }
    static String selection(Context c){return c.getSharedPreferences("scores",Context.MODE_PRIVATE).getString("language","system");}
    static boolean hebrew(Context c){return "he".equals(language(c));}
    static void direction(View v,Context c){v.setLayoutDirection(hebrew(c)?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);}
    static String t(Context c,String key){
        String l=language(c);
        if("ru".equals(l)) return ru(key);
        if("he".equals(l)) return he(key);
        return en(key);
    }
    static String encouragement(Context c,int level){String[] a="ru".equals(language(c))?WIN_RU:"he".equals(language(c))?WIN_HE:WIN_EN;return a[Math.max(0,level-1)%a.length];}
    static String consolation(Context c,int score){String[] a="ru".equals(language(c))?LOSE_RU:"he".equals(language(c))?LOSE_HE:LOSE_EN;return a[Math.abs(score/100)%a.length];}
    private static String en(String k){switch(k){
        case "system":return "System language";
        case "level":return "LEVEL";case "settings":return "SETTINGS";case "about":return "ABOUT";case "pause":return "PAUSED";case "won":return "LEVEL COMPLETE";case "over":return "GAME OVER";case "title":return "PADDLEBEAM ARCADE";case "start":return "Tap to start";case "continue":return "Tap to continue";case "next":return "Tap for next level";case "new":return "Tap for a new game";case "drag":return "Drag your finger to move the paddle";case "theme":return "THEME";case "night":return "Night";case "day":return "Day";case "mute":return "Mute sound";case "mute_note":return "Mute disables ball, brick, win, and game-over sounds.";case "language":return "LANGUAGE";case "back":return "BACK TO GAME";case "app_version":return "App name and version";case "contact":return "Contact developer";case "email":return "EMAIL DEVELOPER";case "privacy":return "Privacy information";case "privacy_text":return "This game works offline. It does not request an account, collect personal information, use advertising identifiers, track location, or share data with third parties. The app stores only the high score locally on your device. This value never leaves the device and can be removed by clearing the app data or uninstalling the app.";case "support":return "SUPPORT THE DEVELOPER";case "support_note":return "Support is voluntary and does not unlock features, remove ads, or provide digital benefits.";default:return k;}}
    private static String ru(String k){switch(k){
        case "system":return "Язык телефона";
        case "level":return "УРОВЕНЬ";case "settings":return "НАСТРОЙКИ";case "about":return "О ПРИЛОЖЕНИИ";case "pause":return "ПАУЗА";case "won":return "УРОВЕНЬ ПРОЙДЕН";case "over":return "ИГРА ОКОНЧЕНА";case "title":return "PADDLEBEAM ARCADE";case "start":return "Коснитесь, чтобы начать";case "continue":return "Коснитесь, чтобы продолжить";case "next":return "Коснитесь — следующий уровень";case "new":return "Коснитесь — новая игра";case "drag":return "Ведите пальцем, чтобы двигать платформу";case "theme":return "ТЕМА";case "night":return "Ночная";case "day":return "Дневная";case "mute":return "Отключить звук";case "mute_note":return "Отключает звуки мяча, блоков, победы и окончания игры.";case "language":return "ЯЗЫК";case "back":return "ВЕРНУТЬСЯ В ИГРУ";case "app_version":return "Название и версия приложения";case "contact":return "Связаться с разработчиком";case "email":return "НАПИСАТЬ РАЗРАБОТЧИКУ";case "privacy":return "Информация о конфиденциальности";case "privacy_text":return "Игра работает офлайн. Она не требует аккаунта, не собирает персональные данные, рекламные идентификаторы или местоположение и не передаёт данные третьим лицам. На устройстве сохраняется только рекорд; его можно удалить, очистив данные приложения или удалив приложение.";case "support":return "ПОДДЕРЖАТЬ РАЗРАБОТЧИКА";case "support_note":return "Поддержка добровольна и не открывает функции, не удаляет рекламу и не предоставляет цифровых преимуществ.";default:return en(k);}}
    private static String he(String k){switch(k){
        case "system":return "שפת המכשיר";
        case "level":return "שלב";case "settings":return "הגדרות";case "about":return "אודות";case "pause":return "השהיה";case "won":return "השלב הושלם";case "over":return "המשחק נגמר";case "title":return "PADDLEBEAM ARCADE";case "start":return "הקש כדי להתחיל";case "continue":return "הקש כדי להמשיך";case "next":return "הקש לשלב הבא";case "new":return "הקש למשחק חדש";case "drag":return "גרור את האצבע כדי להזיז את המחבט";case "theme":return "ערכת נושא";case "night":return "לילה";case "day":return "יום";case "mute":return "השתקת צלילים";case "mute_note":return "משתיק את צלילי הכדור, הלבנים, הניצחון וסיום המשחק.";case "language":return "שפה";case "back":return "חזרה למשחק";case "app_version":return "שם וגרסת האפליקציה";case "contact":return "יצירת קשר עם המפתח";case "email":return "שליחת דוא״ל למפתח";case "privacy":return "מידע על פרטיות";case "privacy_text":return "המשחק פועל במצב לא מקוון. הוא אינו דורש חשבון, אינו אוסף מידע אישי, מזהי פרסום או מיקום ואינו משתף נתונים עם צדדים שלישיים. רק השיא נשמר במכשיר וניתן למחיקה באמצעות ניקוי נתוני האפליקציה או הסרתה.";case "support":return "תמיכה במפתח";case "support_note":return "התמיכה היא מרצון ואינה פותחת תכונות, מסירה פרסומות או מעניקה הטבות דיגיטליות.";default:return en(k);}}
}
