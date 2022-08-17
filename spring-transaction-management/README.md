
#### > Gereklilikler

Java 1.8 +, IDE (Eclipse, IntellejIDEA), Maven 3.0 +, Git, Postman.


#### > H2 database configuration

```SQL:title=data.sql
INSERT INTO account (id, acct_number, balance, name, rate)
VALUES (1, 11111, 200,  'Hamza AFFANI', 0.035);

INSERT INTO account (id, acct_number, balance, name, rate)
VALUES (2, 22222, 200,  'Zakarya AFFANI', 0.035);
```

```java:title=WebConfiguration.java
@Configuration
public class WebConfiguration {
    @Bean
    ServletRegistrationBean h2servletRegistration(){
      // highlight-next-line
        ServletRegistrationBean registrationBean = new ServletRegistrationBean( new WebServlet());
        registrationBean.addUrlMappings("/console/*");
        return registrationBean;
    }
}
```

---



H2 In Memory database, found at http://localhost:8080/console
The jdbc url should be: jdbc:h2:mem:datatranx
The user name is "sa" and there is no password

