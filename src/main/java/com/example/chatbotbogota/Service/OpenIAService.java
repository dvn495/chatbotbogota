package com.example.chatbotbogota.Service;

import java.util.Map;
import java.nio.file.Paths;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.chatbotbogota.Model.PreguntaFrecuente;


@Service
public class OpenIAService {

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model.id}")
    private String modelId;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, String> responseCache = new java.util.concurrent.ConcurrentHashMap<>();

    private final List<PreguntaFrecuente> preguntasFrecuentes = List.of(
            new PreguntaFrecuente("¿Qué es Campuslands y cómo funciona?",
                    "Campuslands es una experiencia educativa intensiva que busca transformar vidas en solo un año, formando a jóvenes en habilidades de tecnología y competencias blandas para integrarse en el sector laboral del futuro. Nos especializamos en programación, inglés y desarrollo personal, todo en un ambiente presencial y 24/7. Aquí, nos enfocamos en brindar una experiencia completa que te prepara para obtener empleos bien remunerados en tecnología, acelerando tu camino hacia una carrera exitosa."),
            new PreguntaFrecuente("¿Cuánto tiempo dura el programa de Campuslands?",
                    "El programa tiene una duración de 10 meses, con un enfoque intensivo de aprendizaje que maximiza tu formación en un tiempo reducido. Para ajustarnos a las necesidades de los estudiantes, Campuslands ofrece dos jornadas: una en horario matutino de 7:00 a.m. a 2:00 p.m. y otra en horario vespertino de 2:00 p.m. a 10:00 - 11:00 p.m., dependiendo de la disponibilidad. Con estas opciones, buscamos que puedas adaptarte al horario que mejor se acomode a tus necesidades sin comprometer la intensidad del programa."),
            new PreguntaFrecuente("¿Qué requisitos necesito para inscribirme en Campuslands?",
                    "En Campuslands, creemos que más allá de los conocimientos previos, las cualidades más importantes que buscamos en nuestros estudiantes son la actitud, la lógica y la disciplina. Valoramos profundamente la actitud positiva y el compromiso con el aprendizaje, la capacidad de razonamiento lógico y la disciplina para afrontar los desafíos del programa intensivo. Si posees estas cualidades, tienes todo lo necesario para embarcarte en esta experiencia transformadora."),
            new PreguntaFrecuente("¿Cuál es el costo del programa y existen facilidades de pago?",
                    "El programa tiene una inversión de 20 millones de pesos, pero en Campuslands estamos comprometidos con hacer la educación accesible. Contamos con planes de becas que cubren entre el 50% y el 100% del costo, además de opciones de financiamiento para quienes califiquen. Te invitamos a agendar una cita en el campus para conocer más sobre las oportunidades de apoyo financiero y ver cómo Campuslands puede ser el primer paso hacia tu futuro en tecnología."),
            new PreguntaFrecuente("¿Qué materias o habilidades voy a aprender en Campuslands?",
                    "En Campuslands, aprenderás programación avanzada, inglés y habilidades blandas esenciales para el mundo laboral. El plan de estudios está diseñado para proporcionarte competencias técnicas en desarrollo de software y habilidades de comunicación en inglés que te abrirán puertas en el mercado global. Además, cultivamos habilidades interpersonales como trabajo en equipo, adaptabilidad y liderazgo, asegurándonos de que seas un profesional completo y preparado para enfrentar los desafíos del mercado."),
            new PreguntaFrecuente("¿Qué nivel de inglés necesito para ser admitido?",
                    "No se requiere un nivel avanzado de inglés para ingresar a Campuslands. Aquí, te ayudaremos a mejorar tus habilidades en el idioma desde lo básico hasta un nivel que te permita destacar en el ámbito laboral. Sabemos que el inglés es fundamental en la tecnología, y por eso incluimos el aprendizaje del idioma en el programa, acompañándote en cada paso para que te sientas seguro y preparado."),
            new PreguntaFrecuente("¿Cómo me ayudará Campuslands a conseguir empleo después de graduarme?",
                    "En Campuslands, nuestro modelo educativo se enfoca no solo en la formación académica, sino en la conexión directa con el mercado laboral. Gracias a nuestras alianzas con empresas y nuestra reputación en la industria, logramos que nuestros estudiantes consigan empleo de calidad en un tiempo récord, en promedio, en solo 90 días después de finalizar la formación. Nuestra metodología orientada a resultados es lo que nos diferencia de cualquier otro modelo educativo."),
            new PreguntaFrecuente("¿Cuál es el retorno de inversión del programa?",
                    "Campuslands ofrece uno de los retornos de inversión más rápidos en el ámbito educativo. Gracias a las oportunidades de empleo bien remuneradas que obtienen nuestros egresados, podrás recuperar tu inversión en menos de un año. Con salarios que duplican el mínimo legal vigente, Campuslands asegura que tu esfuerzo y dedicación se vean reflejados en beneficios económicos concretos en el corto plazo."),
            new PreguntaFrecuente("¿Cuánto ganan en promedio los graduados de Campuslands?",
                    "Los graduados de Campuslands suelen obtener empleos con un salario inicial de al menos dos veces el salario mínimo vigente, y muchos logran incrementos en sus ingresos a medida que avanzan en sus carreras en tecnología. Esta es una inversión que se traduce en estabilidad y crecimiento profesional, permitiéndote mejorar tu calidad de vida y construir un futuro sólido."),
            new PreguntaFrecuente("¿Cuántas horas al día tendré que estudiar?",
                    "El programa es intensivo y requiere un compromiso de 11 horas diarias. Esta intensidad garantiza que puedas absorber todo el conocimiento necesario en un solo año, preparando tu perfil para el mercado laboral en tiempo récord. Este esfuerzo es la clave para lograr resultados impactantes y una transformación profesional completa."),
            new PreguntaFrecuente("¿El programa es solo para personas con conocimientos previos en tecnología?",
                    "No, Campuslands está diseñado para personas sin experiencia previa en tecnología. Nuestro enfoque es inclusivo y accesible para todos los jóvenes que deseen aprender y superarse. Solo necesitas ganas de aprender, compromiso y apertura para enfrentar los retos de una formación intensiva."),
            new PreguntaFrecuente("¿Qué diferencia a Campuslands de una universidad o instituto tradicional?",
                    "Campuslands es una alternativa disruptiva a la educación tradicional. Nuestro modelo intensivo y 100% presencial está orientado a maximizar el aprendizaje práctico, preparándote para el mercado laboral en solo un año. A diferencia de las universidades, aquí encontrarás un enfoque en habilidades prácticas y una conexión directa con oportunidades laborales, acelerando tu crecimiento profesional."),
            new PreguntaFrecuente("¿Puedo estudiar y trabajar al mismo tiempo mientras hago el programa?",
                    "Aunque el programa de Campuslands es intensivo, algunos estudiantes han logrado combinar estudio y trabajo. Esto requiere una alta disciplina y esfuerzo, ya que la carga académica es demandante. Con determinación, es posible hacerlo, y tenemos ejemplos de jóvenes que lo han logrado con éxito, pero debes estar preparado para el compromiso y la dedicación que esto implica."),
            new PreguntaFrecuente("¿El programa es presencial o puedo tomarlo en línea?",
                    "Campuslands es 100% presencial porque creemos que el aprendizaje inmersivo en un ambiente físico es fundamental para tu desarrollo. La interacción directa con mentores, compañeros y el ambiente del campus enriquece tu experiencia y garantiza un aprendizaje más profundo y efectivo."),
            new PreguntaFrecuente("¿Qué apoyo recibo durante el programa si tengo dificultades?",
                    "En Campuslands, contarás con un equipo de mentores, instructores y psicólogos que te acompañarán en cada etapa. Si encuentras dificultades, nuestro equipo estará disponible para ayudarte a superar cualquier obstáculo, asegurándonos de que avances con confianza hacia tus objetivos."),
            new PreguntaFrecuente("¿Hay un proceso de selección para ingresar al programa?",
                    "Sí, en Campuslands contamos con un proceso de selección riguroso. Los candidatos realizan pruebas virtuales y presenciales, y, una vez superada esta etapa, participan en una fase intensiva llamada “Sandbox”. Durante dos semanas, se evalúan habilidades en lógica, actitud y disciplina en un entorno intensivo y presencial con mentores e instructores. Esta inversión en el proceso de selección asegura que los seleccionados están comprometidos y preparados para aprovechar al máximo el programa."),
            new PreguntaFrecuente("¿Campuslands ofrece becas o apoyos financieros?",
                    "Campuslands está comprometido en crear oportunidades de educación accesible para jóvenes talentosos que desean un futuro mejor. Ofrecemos opciones de financiamiento y becas que cubren entre el 50% y el 100% del costo, dependiendo del desempeño y logros del estudiante. Creemos en el potencial de los jóvenes y en su capacidad de cambio, y por eso buscamos que puedan acceder a una educación de calidad sin limitaciones económicas."),
            new PreguntaFrecuente("¿Puedo visitar el campus antes de inscribirme?",
                    "¡Claro! Te invitamos a agendar una visita para que puedas conocer el campus, vivir el ambiente de Campuslands y entender de cerca de nosotros enfoque intensivo de formación. Esta experiencia es ideal para que veas de primera mano cómo podemos ayudarte a construir tu futuro."),
            new PreguntaFrecuente("¿Cómo es la comunidad de estudiantes en Campuslands?",
                    "Campuslands es más que un centro de formación; es una comunidad de jóvenes talentosos que se apoyan mutuamente. Aquí encontrarás compañeros que comparten tus mismas metas de crecimiento y éxito, creando una red de apoyo y colaboración que te acompañará en cada paso del camino."),
            new PreguntaFrecuente("¿Qué tipo de trabajos puedo obtener al graduarme de Campuslands?",
                    "Al finalizar el programa, estarás capacitado para roles como desarrollador de software, analista de datos, soporte técnico y otros puestos demandados en tecnología. Campuslands te abre las puertas a empleos que no solo ofrecen buenas remuneraciones, sino también oportunidades de crecimiento y desarrollo en un sector en constante expansión."),
            new PreguntaFrecuente("¿Qué título se obtiene al salir de Campuslands?",
                    "Al graduarte de Campuslands, recibirás un certificado oficial de Técnico Laboral en Desarrollo de Software, respaldado por la Secretaría de Educación. Este título te posiciona como un profesional preparado para afrontar los retos del sector tecnológico, con conocimientos en programación, inglés y habilidades interpersonales que te abren puertas a oportunidades laborales de calidad."),
            new PreguntaFrecuente("¿Qué lenguajes de programación/temas/materias se aprenden en Campuslands?",
                    "En Campuslands, actualmente puedes elegir entre dos rutas de aprendizaje: la ruta Java, donde aprenderás Python, HTML, CSS, MySQL, Java y Spring Boot, y la ruta Node, que incluye Python, HTML, CSS, MySQL, Node.js y Express. Además, ambas rutas se complementan con clases de inglés y desarrollo de habilidades socioemocionales para que te formes como un profesional integral y preparado para el mercado laboral."),
            new PreguntaFrecuente("¿Dónde está ubicado Campuslands?",
                    "Nuestra sede está ubicada en la Universidad EAN, calle 71 #9-84."),
            new PreguntaFrecuente("¿Me puedo comunicar con alguien de Campuslands?",
                    "¡Claro que sí! Si deseas comunicarte con nosotros directamente, te recomiendo primero realizar tu registro en el siguiente enlace, lo cual nos permitirá conocer tus intereses y ofrecerte la mejor atención: https://miniurl.cl/RegistroCampuslands. Una vez registrado, recibirás noticias nuestras muy pronto. Si prefieres otro medio, también puedes escribirnos por WhatsApp al +57 317 7709345. Ten en cuenta que la respuesta puede tomar un poco de tiempo, pero garantizamos que recibirás respuesta lo antes posible."),
            new PreguntaFrecuente("¿Cómo es el equipo docente/qué profesores tiene Campuslands?",
                    "En Campuslands, contamos con un equipo de instructores altamente capacitados y apasionados, que no solo son expertos en sus áreas, sino que también tienen experiencia en la industria tecnológica. Nuestros mentores se especializan en desarrollo de software, habilidades digitales avanzadas y comunicación en inglés, proporcionando un enfoque práctico y actualizado que responde a las demandas del mercado laboral."),
            new PreguntaFrecuente("¿Cuánto dura el proceso de empleabilidad en Campuslands?",
                    "En cuanto al proceso de empleabilidad, Campuslands se enfoca en conectar a sus egresados con oportunidades laborales en el sector tecnológico de manera rápida y efectiva. En promedio, nuestros egresados logran conseguir su primer empleo en tecnología en un tiempo récord, usualmente en solo 90 días después de completar el programa. Este éxito se debe a nuestras alianzas con empresas y el enfoque práctico de la formación, que hace que los graduados estén listos para enfrentar los desafíos del mercado laboral desde el primer día."),
            new PreguntaFrecuente("¿Cuál es la probabilidad de conseguir empleo al finalizar el programa?",
                    "Campuslands garantiza un acceso rápido al mercado laboral, con el 80% de sus egresados consiguiendo su primer empleo en el sector tecnológico en un promedio de 90 días después de finalizar su formación."),
            new PreguntaFrecuente("¿Es rentable la inversión en el programa de Campuslands?",
                    "Sí, la inversión en Campuslands es rentable en tiempo récord. En solo un año, los estudiantes suelen recuperar su inversión inicial gracias a los salarios competitivos en el sector tecnológico, lo que transforma sus vidas tanto a nivel personal como profesional."),
            new PreguntaFrecuente("¿Qué ventajas ofrece Campuslands frente al sistema educativo tradicional?",
                    "Campuslands ofrece varias ventajas especiales: Rapidez en la transformación, con 2100 horas intensivas los jóvenes logran acceder al mercado laboral en un año, mientras que en el sistema tradicional el proceso lleva entre 3 y 5 años. Accesibilidad e Inclusión Social, Campuslands está diseñado para jóvenes de estratos 1, 2 y 3, con un modelo económico y un retorno de inversión rápido, en contraste con el sistema tradicional, que suele ser más costoso. Educación 100% Presencial, ideal para jóvenes que carecen de acceso a tecnología o un ambiente de estudio adecuado en sus hogares, cerrando la brecha digital. Formación práctica y relevante, el enfoque de Campuslands está en competencias prácticas como programación e inglés, adecuadas para el mercado global, a diferencia de los enfoques teóricos del sistema tradicional."),
            new PreguntaFrecuente("¿Qué conocimientos técnicos adquieren los estudiantes en Campuslands?",
                    "El programa incluye: Teoría en programación (800 horas): Base sólida en lenguajes como JavaScript y Python, desarrollo web, bases de datos y algoritmos. Práctica en programación (800 horas): Desarrollo de proyectos y aplicaciones reales que simulan problemas del entorno laboral."),
            new PreguntaFrecuente("¿Qué nivel de inglés alcanzan los egresados?",
                    "Los estudiantes adquieren un nivel intermedio (B1/B2), que les permite participar en conversaciones técnicas y trabajar en equipos internacionales, una habilidad muy demandada en empresas de tecnología."),
            new PreguntaFrecuente("¿Qué habilidades blandas desarrollan los estudiantes?",
                    "Durante el programa, los jóvenes trabajan en habilidades interpersonales y de liderazgo, como la comunicación efectiva, la resolución de problemas, el trabajo en equipo y la adaptabilidad, apoyados por psicólogos y especialistas en desarrollo personal."),
            new PreguntaFrecuente("¿Cómo es el perfil final de un egresado de Campuslands?",
                    "El egresado sale como Desarrollador Junior, listo para trabajar en equipos de desarrollo, con competencias técnicas, habilidades de comunicación en inglés y una sólida base en habilidades blandas, lo que lo hace un candidato ideal para empresas en el sector tecnológico.")
    );



    public String getCustomGPTResponse(String userMessage) {

        if (responseCache.containsKey(userMessage)) {
            return responseCache.get(userMessage);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        StringBuilder preguntasContent = new StringBuilder();
        for (PreguntaFrecuente pregunta: preguntasFrecuentes){
            preguntasContent.append("Pregunta: ").append(pregunta.getPregunta()).append("\n");
            preguntasContent.append("Respuesta: ").append(pregunta.getRespuesta()).append("\n\n");
        }


        Map<String, Object> body = Map.of(
                "model", modelId,
                "messages", List.of(
                        Map.of("role", "system", "content", "Eres Campi, un asistente amigable que ayuda a entender Campuslands. Responde de forma cálida y detallada, lo mas consiso con un maximo de 100 tokens, pero sin mencionar otras instituciones educativas o comparaciones, responde con emojis, ademas de esto usa la siguiente informacion para responder cualquier pregunta."+ preguntasContent.toString()),
                        Map.of("role", "user", "content", userMessage)
                ),
                "max_tokens", 150,
                "temperature", 0.2
        );


        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            String response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            ).getBody();

            String IAResponse = parseResponse(response);
            responseCache.put(userMessage, IAResponse);
            return IAResponse;
        } catch (Exception e) {
            System.err.println("Error al obtener respuesta de OpenAI: " + e.getMessage());
            e.printStackTrace();
            return "Error al obtener respuesta de la IA. Inténtalo más tarde.";
        }
    }


    private String parseResponse(String response) {
        try {
            org.json.JSONObject jsonResponse = new org.json.JSONObject(response);
            return jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        } catch (Exception e) {
            System.err.println("Error al parsear respuesta de Chat GPT: " + e.getMessage());
            return "Error en la respuesta de la IA. Inténtalo más tarde.";
        }
    }


}