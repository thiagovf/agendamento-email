# Tabela de conteúdos
1. [Agendamento Email](#agendamento-email)
    1. [Contextualização Jakarta EE](contextualização-jakarta-ee)
    2. [Uso do Singleton](#uso-do-singleton)
2. [Uso de Fila](#uso-de-fila)
    1. [Configurando fila no Wildfly](#configurando-fila-no-wildfly)
       1. [Mudança no código](#mudança-no-código)
       2. [Verificando se o agendamento funcionou](#verificando-se-o-agendamento-funcionou)
    2. [Criação do consumer](#criação-do-consumer )
# Agendamento Email
Projeto EJB com Jakarta EE, refente a um curso.  
## Contextualização Jakarta EE
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
## Uso de Fila
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
### Mudança no código
Na classe AgendamentoEmailJob, precisaremos fazer alguns ajustes.
```java
@Singleton
public class AgendamentoEmailJob {
	
	@Inject
	private AgendamentoEmailServico agendamentoEmailServico;
	
	@Inject
	@JMSConnectionFactory("java:jboss/DefaultJMSConnectionFactory")
	private JMSContext context;
	
	@Resource(mappedName = "java:/jms/queue/EmailQueue")
	private Queue queue;

	@Schedule(hour = "*", minute = "*", second = "*/10")
	public void enviarEmails() {
		List<AgendamentoEmail> listaAgendamentos = agendamentoEmailServico.listarPorNaoAgendado();
		listaAgendamentos.forEach(agendamentoNaoEnviado -> {
			context.createProducer().send(queue, agendamentoNaoEnviado);
			agendamentoEmailServico.alterar(agendamentoNaoEnviado);
		});
	}
}
```
* O @JMSConnectionFactory vai definir a *fábrica de conexão* da fila que iremos usar a padrão que já vem configurada "java:jboss/DefaultJMSConnectionFactory", representada no código pelo JMSContext.  
* Também adicionamos a fila recuperando através do @Resource pelo JNDI que criamos anteriormente.
* Por fim, cria um produtor e coloca na fila o e-mail para ser enviado através do trecho ```context.createProducer().send(queue, agendamentoNaoEnviado);```  
### Verificando se o agendamento funcionou
Com o servidor inicializado, acessar o [administrativo do Wildfly](http://127.0.0.1:9990).  
![wildfly-admin](https://github.com/thiagovf/agendamento-email/blob/master/wildfly-admin.png?raw=true)
## Uso do @Schedule 
Nossa aplicação deve ser executada a cada 10 segundos, para isso, utilizamos a anotação javax.ejb.Schedule. Através dela, é possível definir manualmente o código, como no trecho abaixo.
```java
@Schedule(hour = "*", minute = "*", second = "*/10")
public void enviarEmails() {
	List<AgendamentoEmail> listaAgendamentos = agendamentoEmailServico.listarPorNaoAgendado();
	listaAgendamentos.forEach(agendamentoNaoEnviado -> {
		context.createProducer().send(queue, agendamentoNaoEnviado);
		agendamentoEmailServico.alterar(agendamentoNaoEnviado);
	});
}
```  
No curso, fizemos uso da anotação @Schedule para que a tarefa seja executada constantemente. Essa parametrização por anotation tem sua utilidade, mas, no ambiente profissional, esbarrou com a necessidade de recuperar a parametrização do banco de dados, fazendo com que ela possa ser setada sem a necessidade de intervenção direta no código. Para isso, usei a classe ```javax.ejb.TimerService```. Conforme o trecho abaixo, ela permite fazer a definição dinâmica.
```java
@Singleton
@Startup
public class AgendamentEmailJobParametrizavel {

	private static String VARIAVEL_TEMPO_BD = "hora.job.apoiamento";

	@Resource
	private TimerService timerService;
	
	@Inject
	ParametroSistemaManager parametroSistemaManager;
	
	@Inject
	private AgendamentoEmailServico agendamentoEmailServico;
	
	@Inject
	@JMSConnectionFactory("java:jboss/DefaultJMSConnectionFactory")
	private JMSContext context;
	
	@Resource(mappedName = "java:/jms/queue/EmailQueue")
	private Queue queue;
	
	@PostConstruct
	public void init() {
		createTimer();
	}

	@Timeout
	public void timerTimeout() {
		executarVerificacao();
	}

	private void createTimer() {
		ParametroSistema parametro = parametroSistemaManager.consultar(VARIAVEL_TEMPO_BD);

		if(parametro != null) {
			LocalTime tempoIntervalo = LocalTime.parse(parametro.getParametro());
			ScheduleExpression scheduleExpression = new ScheduleExpression();
			scheduleExpression
				.second(tempoIntervalo.getSecond())
				.minute(tempoIntervalo.getMinute())
				.hour(tempoIntervalo.getHour());
			TimerConfig timerConfig = new TimerConfig();
			timerConfig.setPersistent(false);
			timerService.createCalendarTimer(scheduleExpression, timerConfig);
		} else {
			StringBuilder mensagem = new StringBuilder();
			mensagem.append("Parametro ")
				.append(VARIAVEL_TEMPO_BD)
				.append(" não informado no Banco de Dados.");
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, mensagem.toString());
		}
	}

	public void executarVerificacao() {
		List<AgendamentoEmail> listaAgendamentos = agendamentoEmailServico.listarPorNaoAgendado();
		listaAgendamentos.forEach(agendamentoNaoEnviado -> {
			context.createProducer().send(queue, agendamentoNaoEnviado);
			agendamentoEmailServico.alterar(agendamentoNaoEnviado);
		});
	}
}
```
## Criação do consumer 
O consumidor é que  vai realmente enviar o e-mail. Ele irá recuperar da fila e enviar o e-mail. Para isso, utilizamos o [Message-Driven Bean (MDB)](https://docs.oracle.com/cd/A97688_16/generic.903/a97677/mdb.htm) que irá permitir abstrair várias implementações no consumo da fila.  
![MDB-Oracle](https://docs.oracle.com/cd/A97688_16/generic.903/a97677/mdba.gif)  
A ideia do MDB (código abaixo) é que ele fique escutando e quando tiver mensagem na fila, ele envie o e-mail para os destinatários.  
```java  
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/jms/queue/EmailQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue")
})
public class AgendamentoEmailMDB implements MessageListener {

	@Inject
	private AgendamentoEmailServico servico;

	@Override
	public void onMessage(Message message) {
		try {
			AgendamentoEmail agendamentoEmail = message.getBody(AgendamentoEmail.class);
			servico.enviar(agendamentoEmail);
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}
}
```
* A anotação ```@MessageDriven``` vai definir a classe como um MDB. 
* Dentro dela, precisamos fazer algumas configurações através do parâmetro ```activationConfig``` para definir qual a fila que vai ser escutada ```@ActivationConfigProperty(propertyName = "destinationLookup"...```. 
* A outra propriedade ainda dentro do ```@ActivationConfigProperty```vai definir o tipo de fila ```propertyName = "destinationType"```. Nesse curso, estamos vendo o tipo Queue. Existem outros.
* Feito isso, precisamos transformar a Message no tipo ```AgendamentoEmail```.  
```java  
AgendamentoEmail agendamentoEmail = message.getBody(AgendamentoEmail.class);
servico.enviar(agendamentoEmail);
```
## Controle trasancional
Como estamos falando de uma aplicação que irá recuperar uma lista de e-mails a ser enviadas, caso ocorra algum erro no envio de um e-mail talvez não faça sentido que todos os demais e-mails da fila não sejam agendados. Dessa forma, é importante pensar bem a estratégia transacional.  
### Controle manual
O JTA oferece duas formas de demarcação de transações, Bean-Managed (BMT), e Container-Managed (CMT). Ao invés de deixarmos o controle transacional do DAO a critério do container (comportamento default, CMT), iremos utilizar a anotação ```@TransactionManagement(TransactionManagementType.BEAN)``` pra definir que o controle vai ser feito pelo bean, injetar o ```UserTransaction``` em todo método que faz mudança no banco e, a partir daí, teremos que iniciar a transação e fechá-la, conforme o método alterar no trecho de código abaixo.
```java
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class AgendamentoEmailDAO {

	@PersistenceContext
	private EntityManager em;
	
	@Inject
	private UserTransaction userTransaction;
	
	public void alterar(AgendamentoEmail agendamentoEmail) {
		try {
			userTransaction.begin();
			em.merge(agendamentoEmail);
			userTransaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
} 
```  
Bom ressaltar que o controle manual teria que ser em um cenário bem específico em que valesse a pena. No exemplo acima, caso trivial, não valeria fazer esse controle manual já que o container resolve isso muito bem pra gente.
## Envio de e-mail
Durante o curso, utilizamos apenas um ```Thread.sleep(5000);``` para simular o envio do e-mail. Agora faremos as mudanças para que o e-mail seja enviado realmente através do [Jakarta Mail 1.6](https://jakarta.ee/specifications/mail/1.6/), biblioteca usada para enviar e eceber emails independente de protocolo (SMTP, POP3,IMAP). A versão 1.6 é a que é compatível com a Jakarta EE 8 que estamos utilizando.
