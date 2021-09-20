//package br.com.email.job;
//
//import java.time.LocalTime;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.Resource;
//import javax.ejb.ScheduleExpression;
//import javax.ejb.Singleton;
//import javax.ejb.Startup;
//import javax.ejb.Timeout;
//import javax.ejb.TimerConfig;
//import javax.ejb.TimerService;
//import javax.inject.Inject;
//import javax.jms.JMSConnectionFactory;
//import javax.jms.JMSContext;
//import javax.jms.Queue;
//
//import br.com.email.entidade.AgendamentoEmail;
//import br.com.email.servico.AgendamentoEmailServico;
//
//@Singleton
//@Startup
//public class AgendamentEmailJobParametrizavel {
//
//	private static String VARIAVEL_TEMPO_BD = "hora.job.apoiamento";
//
//	@Resource
//	private TimerService timerService;
//	
//	@Inject
//	ParametroSistemaManager parametroSistemaManager;
//	
//	@Inject
//	private AgendamentoEmailServico agendamentoEmailServico;
//	
//	@Inject
//	@JMSConnectionFactory("java:jboss/DefaultJMSConnectionFactory")
//	private JMSContext context;
//	
//	@Resource(mappedName = "java:/jms/queue/EmailQueue")
//	private Queue queue;
//	
//	@PostConstruct
//	public void init() {
//		createTimer();
//	}
//
//	@Timeout
//	public void timerTimeout() {
//		executarVerificacao();
//	}
//
//	private void createTimer() {
//		ParametroSistema parametro = parametroSistemaManager.consultar(VARIAVEL_TEMPO_BD);
//
//		if(parametro != null) {
//			LocalTime tempoIntervalo = LocalTime.parse(parametro.getParametro());
//			ScheduleExpression scheduleExpression = new ScheduleExpression();
//			scheduleExpression
//				.second(tempoIntervalo.getSecond())
//				.minute(tempoIntervalo.getMinute())
//				.hour(tempoIntervalo.getHour());
//			TimerConfig timerConfig = new TimerConfig();
//			timerConfig.setPersistent(false);
//			timerService.createCalendarTimer(scheduleExpression, timerConfig);
//		} else {
//			StringBuilder mensagem = new StringBuilder();
//			mensagem.append("Parametro ")
//				.append(VARIAVEL_TEMPO_BD)
//				.append(" não informado no Banco de Dados.");
//			Logger.getLogger(getClass().getName()).log(Level.SEVERE, mensagem.toString());
//		}
//	}
//
//	public void executarVerificacao() {
//		List<AgendamentoEmail> listaAgendamentos = agendamentoEmailServico.listarPorNaoAgendado();
//		listaAgendamentos.forEach(agendamentoNaoEnviado -> {
//			context.createProducer().send(queue, agendamentoNaoEnviado);
//			agendamentoEmailServico.alterar(agendamentoNaoEnviado);
//		});
//	}
//}