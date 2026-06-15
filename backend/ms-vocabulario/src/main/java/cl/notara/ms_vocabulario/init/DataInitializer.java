package cl.notara.ms_vocabulario.init;

import cl.notara.ms_vocabulario.models.Categoria;
import cl.notara.ms_vocabulario.models.Dificultad;
import cl.notara.ms_vocabulario.models.Palabra;
import cl.notara.ms_vocabulario.repositories.PalabraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Juego: se muestra la definición en español → el usuario escribe la palabra en inglés.
 * Campos: palabra = English word | definicion = descripción en español | pista = ayuda.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final PalabraRepository repo;

    public DataInitializer(PalabraRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) {
            log.info("[Vocabulario] Base de datos ya tiene palabras. Se omite la inicialización.");
            return;
        }
        log.info("[Vocabulario] Cargando vocabulario de inglés...");
        repo.saveAll(palabrasIniciales());
        log.info("[Vocabulario] {} palabras cargadas.", repo.count());
    }

    private List<Palabra> palabrasIniciales() {
        return List.of(

            // ─── BASICO — expresiones y palabras del día a día ───────────────
            p("hello",       "Saludo que se usa al encontrarse con alguien.",                       "H _ _ _ _",                       Categoria.BASICO, Dificultad.FACIL),
            p("goodbye",     "Lo que dices cuando te despides de alguien.",                          "G _ _ _ _ _ _",                   Categoria.BASICO, Dificultad.FACIL),
            p("please",      "Palabra de cortesía que acompaña una petición o ruego.",               "P _ _ _ _ _",                     Categoria.BASICO, Dificultad.FACIL),
            p("sorry",       "Disculpa que pides cuando cometes un error o causas daño.",            "Empieza por S",                   Categoria.BASICO, Dificultad.FACIL),
            p("help",        "Asistencia que se presta a alguien en dificultad.",                    "H _ _ _",                         Categoria.BASICO, Dificultad.FACIL),
            p("water",       "Líquido transparente e inodoro esencial para vivir.",                  "W _ _ _ _",                       Categoria.BASICO, Dificultad.FACIL),
            p("friend",      "Persona con quien tienes una relación de amistad cercana.",            "F _ _ _ _ _",                     Categoria.BASICO, Dificultad.MEDIO),
            p("beautiful",   "Adjetivo que describe algo o alguien estéticamente muy agradable.",    "Empieza por B, 9 letras",         Categoria.BASICO, Dificultad.MEDIO),
            p("together",    "Adverbio que indica que dos o más personas están en el mismo lugar.",  "T _ _ _ _ _ _ _",                 Categoria.BASICO, Dificultad.MEDIO),
            p("although",    "Conjunción que introduce una contradicción, equivale a 'aunque'.",     "Empieza por A, 8 letras",         Categoria.BASICO, Dificultad.DIFICIL),

            // ─── HOGAR — vocabulario del hogar ───────────────────────────────
            p("kitchen",     "Habitación de la casa donde se prepara y cocina la comida.",           "K _ _ _ _ _ _",                   Categoria.HOGAR, Dificultad.FACIL),
            p("bedroom",     "Habitación de la casa donde se duerme.",                               "B _ _ _ _ _ _",                   Categoria.HOGAR, Dificultad.FACIL),
            p("bathroom",    "Habitación con ducha, lavamanos e inodoro.",                           "B _ _ _ _ _ _ _",                 Categoria.HOGAR, Dificultad.FACIL),
            p("window",      "Abertura en la pared que permite que entre luz y aire.",               "W _ _ _ _ _",                     Categoria.HOGAR, Dificultad.FACIL),
            p("furniture",   "Término colectivo para los muebles que hay en una habitación.",        "F _ _ _ _ _ _ _ _",               Categoria.HOGAR, Dificultad.MEDIO),
            p("ceiling",     "Parte superior de una habitación, lo opuesto al suelo.",               "C _ _ _ _ _ _",                   Categoria.HOGAR, Dificultad.MEDIO),
            p("chimney",     "Conducto vertical por donde sale el humo de una chimenea.",            "C _ _ _ _ _ _",                   Categoria.HOGAR, Dificultad.MEDIO),
            p("wardrobe",    "Mueble grande donde se cuelga y guarda la ropa.",                      "W _ _ _ _ _ _ _",                 Categoria.HOGAR, Dificultad.MEDIO),
            p("curtains",    "Telas que cuelgan de las ventanas para dar privacidad o bloquear luz.", "C _ _ _ _ _ _ _",                Categoria.HOGAR, Dificultad.MEDIO),
            p("threshold",   "El borde inferior del marco de una puerta, lo que pisas al entrar.",   "Empieza por TH, 9 letras",        Categoria.HOGAR, Dificultad.DIFICIL),

            // ─── ANIMALES — vocabulario de animales ──────────────────────────
            p("dog",         "Animal doméstico conocido como el mejor amigo del hombre.",            "D _ _",                           Categoria.ANIMALES, Dificultad.FACIL),
            p("cat",         "Animal doméstico que maúlla y caza ratones.",                          "C _ _",                           Categoria.ANIMALES, Dificultad.FACIL),
            p("elephant",    "Animal terrestre más grande del mundo, tiene trompa y orejas enormes.", "E _ _ _ _ _ _ _",                Categoria.ANIMALES, Dificultad.FACIL),
            p("giraffe",     "Animal africano con el cuello más largo de todos los mamíferos.",      "G _ _ _ _ _ _",                   Categoria.ANIMALES, Dificultad.FACIL),
            p("butterfly",   "Insecto con alas de colores que vuela de flor en flor.",               "B _ _ _ _ _ _ _ _",               Categoria.ANIMALES, Dificultad.MEDIO),
            p("dolphin",     "Mamífero marino muy inteligente que vive en el océano.",               "D _ _ _ _ _ _",                   Categoria.ANIMALES, Dificultad.MEDIO),
            p("penguin",     "Ave que no puede volar, vive en zonas frías y camina erguida.",        "P _ _ _ _ _ _",                   Categoria.ANIMALES, Dificultad.MEDIO),
            p("kangaroo",    "Mamífero australiano que lleva a sus crías en una bolsa abdominal.",   "K _ _ _ _ _ _ _",                 Categoria.ANIMALES, Dificultad.MEDIO),
            p("chameleon",   "Reptil que puede cambiar de color para camuflarse en su entorno.",     "C _ _ _ _ _ _ _ _",               Categoria.ANIMALES, Dificultad.DIFICIL),
            p("platypus",    "Mamífero australiano con pico de pato que pone huevos.",               "P _ _ _ _ _ _ _",                 Categoria.ANIMALES, Dificultad.DIFICIL),

            // ─── ALIMENTOS — comida y bebida ─────────────────────────────────
            p("bread",       "Alimento básico hecho de harina, agua y levadura horneada.",           "B _ _ _ _",                       Categoria.ALIMENTOS, Dificultad.FACIL),
            p("apple",       "Fruta redonda, puede ser roja o verde, muy común en Europa.",          "A _ _ _ _",                       Categoria.ALIMENTOS, Dificultad.FACIL),
            p("chicken",     "Carne de ave de corral muy consumida en todo el mundo.",               "C _ _ _ _ _ _",                   Categoria.ALIMENTOS, Dificultad.FACIL),
            p("cucumber",    "Vegetal verde y alargado que se come habitualmente en ensaladas.",      "C _ _ _ _ _ _ _",                 Categoria.ALIMENTOS, Dificultad.FACIL),
            p("strawberry",  "Fruta roja y pequeña con pepitas en la superficie.",                   "S _ _ _ _ _ _ _ _ _",             Categoria.ALIMENTOS, Dificultad.MEDIO),
            p("mushroom",    "Hongo comestible con forma de paraguas que crece en el suelo.",        "M _ _ _ _ _ _ _",                 Categoria.ALIMENTOS, Dificultad.MEDIO),
            p("avocado",     "Fruta verde por dentro con una semilla grande en el centro.",          "A _ _ _ _ _ _",                   Categoria.ALIMENTOS, Dificultad.MEDIO),
            p("pineapple",   "Fruta tropical de cáscara dura con corona de hojas verdes.",           "P _ _ _ _ _ _ _ _",               Categoria.ALIMENTOS, Dificultad.MEDIO),
            p("aubergine",   "Vegetal de color morado oscuro, en español se llama berenjena.",       "A _ _ _ _ _ _ _ _",               Categoria.ALIMENTOS, Dificultad.DIFICIL),
            p("coriander",   "Hierba aromática verde muy usada en cocinas latinas y asiáticas.",     "C _ _ _ _ _ _ _ _",               Categoria.ALIMENTOS, Dificultad.DIFICIL),

            // ─── VERBOS — verbos comunes en inglés ───────────────────────────
            p("run",         "Desplazarse a gran velocidad usando las piernas.",                     "R _ _",                           Categoria.VERBOS, Dificultad.FACIL),
            p("swim",        "Desplazarse en el agua usando el cuerpo.",                             "S _ _ _",                         Categoria.VERBOS, Dificultad.FACIL),
            p("laugh",       "Expresar alegría o diversión mediante sonidos y gestos faciales.",     "L _ _ _ _",                       Categoria.VERBOS, Dificultad.FACIL),
            p("understand",  "Captar y comprender el significado de algo que se dice o lee.",        "U _ _ _ _ _ _ _ _ _",             Categoria.VERBOS, Dificultad.MEDIO),
            p("remember",    "Traer algo del pasado a la mente de manera consciente.",               "R _ _ _ _ _ _ _",                 Categoria.VERBOS, Dificultad.MEDIO),
            p("achieve",     "Lograr un objetivo o meta con esfuerzo y dedicación.",                 "A _ _ _ _ _ _",                   Categoria.VERBOS, Dificultad.MEDIO),
            p("whisper",     "Hablar en voz muy baja para que solo el receptor escuche.",            "W _ _ _ _ _ _",                   Categoria.VERBOS, Dificultad.MEDIO),
            p("stumble",     "Tropezar o perder el equilibrio al caminar.",                          "S _ _ _ _ _ _",                   Categoria.VERBOS, Dificultad.DIFICIL),
            p("procrastinate","Posponer tareas o responsabilidades sin una razón válida.",           "P _ _ _ _ _ _ _ _ _ _ _ _",       Categoria.VERBOS, Dificultad.DIFICIL),
            p("overwhelm",   "Hacer que alguien se sienta superado por algo muy intenso.",           "O _ _ _ _ _ _ _ _",               Categoria.VERBOS, Dificultad.DIFICIL),

            // ─── AVANZADO — vocabulario avanzado ─────────────────────────────
            p("ambiguous",   "Que puede interpretarse de más de una manera o con doble sentido.",    "A _ _ _ _ _ _ _ _",               Categoria.AVANZADO, Dificultad.MEDIO),
            p("eloquent",    "Que se expresa de forma clara, precisa y muy convincente.",            "E _ _ _ _ _ _ _",                 Categoria.AVANZADO, Dificultad.MEDIO),
            p("inevitable",  "Que no puede evitarse ni impedirse de ninguna manera.",                "I _ _ _ _ _ _ _ _ _",             Categoria.AVANZADO, Dificultad.MEDIO),
            p("perseverance","Constancia y firmeza para continuar y conseguir un objetivo.",         "P _ _ _ _ _ _ _ _ _ _ _",         Categoria.AVANZADO, Dificultad.MEDIO),
            p("sophisticated","Muy refinado, complejo o de nivel avanzado.",                         "S _ _ _ _ _ _ _ _ _ _ _ _",       Categoria.AVANZADO, Dificultad.DIFICIL),
            p("entrepreneur","Persona que crea y gestiona su propio negocio asumiendo riesgos.",     "Empieza por E, 12 letras",        Categoria.AVANZADO, Dificultad.DIFICIL),
            p("phenomenon",  "Hecho o evento observable que resulta extraordinario o inusual.",      "P _ _ _ _ _ _ _ _ _",             Categoria.AVANZADO, Dificultad.DIFICIL),
            p("conscientious","Que hace las cosas con cuidado, responsabilidad y mucha atención.",   "C _ _ _ _ _ _ _ _ _ _ _ _",       Categoria.AVANZADO, Dificultad.DIFICIL),
            p("quintessential","Que representa el ejemplo más perfecto o típico de algo.",           "Q _ _ _ _ _ _ _ _ _ _ _ _ _",     Categoria.AVANZADO, Dificultad.DIFICIL),
            p("serendipity", "Hallazgo afortunado que se hace de manera accidental e inesperada.",   "S _ _ _ _ _ _ _ _ _",             Categoria.AVANZADO, Dificultad.DIFICIL)
        );
    }

    private Palabra p(String palabra, String definicion, String pista, Categoria cat, Dificultad dif) {
        Palabra w = new Palabra();
        w.setPalabra(palabra);
        w.setDefinicion(definicion);
        w.setPista(pista);
        w.setCategoria(cat);
        w.setDificultad(dif);
        return w;
    }
}
