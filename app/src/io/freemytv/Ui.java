package io.freemytv;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fonts and text for the home screen, the screensaver and the settings. Font: Inter (variable, assets/fonts, SIL OFL),
 * with the "display" optical size for large text and the "text" size for small text; without it, the system font at a
 * similar weight.
 */
final class Ui {
    private Ui() {}

    static final Locale PT = new Locale("pt", "BR");
    static final Locale TH = new Locale("th", "TH");
    /** English, Portuguese, Spanish or Thai, from the settings or, with "auto", the TV's own language. */
    static volatile String language = language();
    static volatile boolean en = "en".equals(language), es = "es".equals(language), th = "th".equals(language);
    static Typeface thin, light, displayMedium, displaySemibold, regular, medium, semibold;
    private static String loadedFont;
    private static final Rect BOUNDS = new Rect();
    private static final RectF OVAL = new RectF();

    static synchronized void init(Context c) {
        String want = Prefs.font();
        if (want.equals(loadedFont)) return;
        loadedFont = want;
        AssetManager a = c.getAssets();
        thin = font(a, want, 32, 200);
        light = font(a, want, 32, 300);
        displayMedium = font(a, want, 32, 500);
        displaySemibold = font(a, want, 32, 600);
        regular = font(a, want, 14, 400);
        medium = font(a, want, 14, 500);
        semibold = font(a, want, 14, 600);
    }

    /** Chosen font family at an optical size (14 = text, 32 = display) and weight; the system font if missing. */
    private static Typeface font(AssetManager a, String choice, int opticalSize, int weight) {
        String path;
        String variation = null;
        switch (choice) {
            case "google":
                path = "fonts/GoogleSansVariable.ttf";
                variation = "'opsz' " + (opticalSize >= 24 ? 18 : 17) + ", 'wght' " + weight;
                break;
            case "anuphan":
                path = "fonts/AnuphanVariable.ttf";
                variation = "'wght' " + weight;
                break;
            case "noto":
                path = "fonts/NotoSansThaiVariable.ttf";
                variation = "'wght' " + weight;
                break;
            case "ibm":
                path = weight <= 200 ? "fonts/IBMPlexSansThai-Thin.ttf"
                        : weight == 300 ? "fonts/IBMPlexSansThai-Light.ttf"
                        : weight == 400 ? "fonts/IBMPlexSansThai-Regular.ttf"
                        : weight == 500 ? "fonts/IBMPlexSansThai-Medium.ttf"
                        : "fonts/IBMPlexSansThai-SemiBold.ttf";
                break;
            case "lineseed":
                path = weight <= 300 ? "fonts/LINESeedSansTH_A_Th.ttf"
                        : weight <= 500 ? "fonts/LINESeedSansTH_A_Rg.ttf"
                        : "fonts/LINESeedSansTH_A_Bd.ttf";
                break;
            default:
                path = FONT;
                variation = "'opsz' " + opticalSize + ", 'wght' " + weight;
                break;
        }
        if (has(a, path)) {
            try {
                Typeface.Builder b = new Typeface.Builder(a, path);
                if (variation != null) b.setFontVariationSettings(variation);
                Typeface t = b.build();
                if (t != null) return t;
            } catch (RuntimeException ignored) {
            }
        }
        return Typeface.create(Typeface.DEFAULT, weight, false);
    }

    private static final String FONT = "fonts/InterVariable.ttf";

    /** "th", "pt", "es" or "en": the one picked (TV settings or config.properties), or with "auto" the TV's own, else English. */
    private static String language() {
        String lang = Prefs.language();
        if (!lang.equals("auto")) return lang;
        lang = Locale.getDefault().getLanguage();
        return "th".equals(lang) || "pt".equals(lang) || "es".equals(lang) ? lang : "en";
    }

    /** After the language changes in the settings (and once Prefs can read them). */
    static void applyLanguage() {
        language = language();
        en = "en".equals(language);
        es = "es".equals(language);
        th = "th".equals(language);
    }

    private static boolean has(AssetManager a, String path) {
        try (InputStream in = a.open(path)) {
            return in != null;
        } catch (IOException e) {
            return false;
        }
    }

    static Paint text(Typeface face, float size, int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        p.setTypeface(face);
        p.setTextSize(size);
        p.setColor(color);
        return p;
    }

    /** Fixed-width digits (the clock and the prices don't jitter when a digit changes). */
    static Paint numbers(Typeface face, float size, int color) {
        Paint p = text(face, size, color);
        p.setFontFeatureSettings("'tnum'");
        return p;
    }

    /** Height of capital letters (and digits) above the baseline. */
    static float cap(Paint p) {
        synchronized (BOUNDS) {
            p.getTextBounds("H8", 0, 2, BOUNDS);
            return -BOUNDS.top;
        }
    }

    /** Half the cap height: added to the center line, it gives the baseline that centers the text. */
    static float halfCap(Paint p) {
        return cap(p) / 2f;
    }

    /** Left bearing of the first character (large, thin fonts start a little inward). */
    static float leftBearing(Paint p, String s) {
        if (s.isEmpty()) return 0;
        synchronized (BOUNDS) {
            p.getTextBounds(s, 0, 1, BOUNDS);
            return BOUNDS.left;
        }
    }

    static int alpha(int color, float a) {
        int base = (color >>> 24) & 255;
        return (Math.round(base * Math.max(0, Math.min(1, a))) << 24) | (color & 0xFFFFFF);
    }

    static float clamp(float v) {
        return Math.max(0, Math.min(1, v));
    }

    static String tr(String pt, String english) {
        return es ? Spanish.text(english) : en ? english : pt;
    }

    static String tr(String pt, String english, String thai) {
        return th ? thai : es ? Spanish.text(english) : en ? english : pt;
    }

    static String tr(String pt, String english, String spanish, String thai) {
        return th ? thai : es ? spanish : en ? english : pt;
    }

    static Locale locale() {
        return th ? Locale.US : es ? spanish() : en ? Locale.US : PT;
    }

    /** The TV's own Spanish (es-MX, es-AR…) when it is in Spanish, Spain's otherwise. */
    private static Locale spanish() {
        Locale tv = Locale.getDefault();
        return "es".equals(tv.getLanguage()) ? tv : new Locale("es", "ES");
    }

    /** "21:47" (in English, "9:47", like the iPhone in the US). */
    static String clock(long now) {
        if (th) return new SimpleDateFormat("HH:mm", TH).format(new Date(now));
        return en ? new SimpleDateFormat("h:mm", Locale.US).format(new Date(now))
                : new SimpleDateFormat("HH:mm", locale()).format(new Date(now));
    }

    /** "5:21" (sunrise and sunset; in English, "5:21 AM"). */
    static String time(long at) {
        if (th) return new SimpleDateFormat("HH:mm", TH).format(new Date(at));
        return en ? new SimpleDateFormat("h:mm a", Locale.US).format(new Date(at))
                : new SimpleDateFormat("H:mm", locale()).format(new Date(at));
    }

    /** "domingo, 18 de outubro" (lowercase, like the iPhone in Portuguese); "Sunday, October 18" in English. */
    static String date(long now) {
        if (th) return new SimpleDateFormat("EEEEที่ d MMMM", TH).format(new Date(now));
        return en ? new SimpleDateFormat("EEEE, MMMM d", Locale.US).format(new Date(now))
                : new SimpleDateFormat("EEEE, d 'de' MMMM", locale()).format(new Date(now));
    }

    /** Spinning arc (work in progress). */
    static void spinner(Canvas c, float cx, float cy, float r, float seconds, Paint stroke) {
        OVAL.set(cx - r, cy - r, cx + r, cy + r);
        c.drawArc(OVAL, (seconds * 360f) % 360f, 270f, false, stroke);
    }
}
