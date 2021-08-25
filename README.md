# Agendamento Email
Projeto EJB com Jakarta EE, refente a um curso.\
Jakarta EE passou a se chamar assim depois que a Oracle liberou para a Eclipse Foudation a manutenção e evolução do Java EE. A Oracle permaneceu com os direitos do nome Java EE, mas liberou sem custo o uso do projeto pela Eclipse. O nome Jakarta era da Apache que mantinha um grupo de projetos (Struts, Tomcat etc) sob esse guarda-chuva e que foi descontinuado em 2011, [Link Apache](https://jakarta.apache.org/site/news/news-2011-q4.html#20111221.1). A Apache também liberou para a Eclipse Foudantion usar o nome Jakarta EE sem custos.

## Uso do Singleton
Passamos a utilizar singleton ([Link Commit](https://github.com/thiagovf/agendamento-email/commit/dc57338322c942f786d331fea75f8ba0b39caba6)) na class AgendamentoEmailJob no envio do e-mail para evitar inconsistências em decorrência de diferentes instâncias mandar e-mail para o mesmo usuário duas vezes.\
A anotation @Singleton substitui toda a lógica de código necessária para implementar um singleton na mão e ainda permite que ela seja gerenciada pelo servidor de aplicação.
```java
// como seria um Singleton na mão.
public class AgendamentoEmailJob {
    private static AgendamentoEmailJob instance;
    private AgendamentoEmailJob() {}
    public synchronized static AgendamentoEmailJob getInstance() {
        if (instance == null) {
            instance = new AgendamentoEmailJob(); 
        }
        return instance;
    }
}
  ```
## Configurando fila no Wildfly
O uso da fila visa evitar que ao ocorrer um erro no envio de um e-mail, a aplicação consiga  enviar os demais e-mails.
### Tornando a classe AgendamentoEmail serializável
Para que seja possível o uso de fila, precisa tornar a classe serializável.
```java
...
public class AgendamentoEmail implements Serializable {

	private static final long serialVersionUID = 1L;
    ...
```
### Configurando o Wildfly
Para o uso de fila, poderíamos usar um repositório externo ou através do servidor de aplicação. No curso, optou-se por esta forma.
1. Inicia o Wildfly pelo Eclipse.
2. Uma vez iniciado, ir até a pasta bin através do terminal dentro da pasta do servidor de aplicação.
3. Inicia o jboss-cli.sh.
4. connect
5. Executar o comando a seguir.
```
jms-queue add --queue-address=EmailQueue --entries=java:/jms/queue/EmailQueue
```
* **--queue-address** define o nome da fila no próprio Wildfly.
* **--entries=java** é a forma como a aplicação vai acessar a fila.


