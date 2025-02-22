# performanceAnalyzerChallenge


Creio que tenho alguns resultados da Analise.

Coloquei na raiz do projecto o ficheiro .odt obtido com o apoio à analise.

Em relação ao resultado da analise, parece-me haver um problema relacionado com o logging do RabbitMQ.
Parece haver um bloqueio (assumindo IO), com async threads, que depois do que assumo ter sido um "restart" que creio ser pelas 2021-03-29 07:33:00, libertou algumas threads bloquedas, em especial a Thread Finalizer (GC), o que por norma leva a alguns problemas de aumento de tempos.
Depois desse momento a carga voltou a aumentar, mas sem grandes bloqueios, acabando por ter a carga reduzida pelas 2012-03-29 08:06.
Parece-me que o restart não resolveu completamente a questão que, uma vez mais me parece estar relacionada com I/O de logging (RabbitLoggingService), por provavel concorrencia aos ficheiros de log de varias instancias, por exemplo.
Pedia também uma nota para que se veja que no grafico, por falta de tempo, não consegui alinhar corretamente as colunas de "BLOCKED" com as restantes. Faltaram-me valores no eixo do X, mas pelo comportamento, é percetivel a "transposição necessária". Com mais algum tempo conseguiria colocar os zeros na Pivot Table e teria esses dados "alinhados".
Peço uma vez mais desculpa pela entrega tardia. 
Se acharem, por ventura, que o trabalho não foi suficientemente bom, está longe do esperado, ou por outro motivo não avançarmos, gostaria de deixar aqui uma nota de que foi extremamente interessante, divertido, e educativo. Obrigado!


//Analise de alguns pontos sobre a estrutura da parte Custom:
A estrutura apresentada tendo dois serviços Springboot recebendo pedidos HTTP, e encaminhando-os via protocolo JOLT para um serviço tuxedo parece-me poder apresentar varios problemas.
Tanto os serviços tuxedo e o protocolo JOLT são novidade para mim, mas fazendo uma pequena pesquisa, consigo perceber alguns potenciais pontos de falha provável, tendo em conta a informação das threads nos ficheiros:
 - A utilização do protocolo JOLT requer a utilização de Connection Pools, que, caso a configuração seja restritiva em termos de numero maximo de ligações, pode facilmente provocar um bottleneck. Também, se estas ligações obtiverem um estado det "idle" por muito tempo, podem gerar Time-outs, que podem gerar erros. Não me recordo de ter visto nada indicativo desta situação, mas alta latencia pode ser causa e efeito.
 - O sistema, por utilização do protocolo JOLT, requer autenticação do client. Recordo-me de ver bastantes casos de threads de filtro (p.e. com.crossjointest.cbs.tuxedo.filter.ApplicationFilter.doFilter(ApplicationFilter.java:52)) em WAITING ou TIMED_WAITING. Isto também pode indicar que parte do bottleneck seja exatamente uma grande quantidade de pedidos a aguardar resposta, do lado do serviço de backend, possivelmente por gestão de rede ou de threads do lado do server.
 - Não consigo perceber, obviamente se algum metodo de recuperação de falhas está ativo, como um caso de retries. Se sim, o ponto dos time-outs pode também estar a afetar o volume de carga.
Dificilmente conseguirei, por aqui apontar mais pontos do que os que já falei. Apenas fazendo uma análise detalhada do sistema do cliente poderia perceber qual ou quais as causas especificas.
Se estivesse no papel da crossJoin, neste caso, provavelmente os meus suspeitos recairiam sobre o nivel de logging de RabbitMQ, bem como a configuração de para onde está a ser escrito esse mesmo log (uma configuração, por exemplo, em que o log esteja a ser redirecionado de todas as instancias para o mesmo ficheiro, num qualquer local, ou outra situação possivelmente critica em termos de I/O), algum processo que estivesse a impactar o garbage collector, a configuração e complexidade dos clientes de JOLT, e por fim configuração do servidor Tuxedo. Talvez considerasse, por prioridade, a ordem apresentada.
Estar a apresentar este tipo de ideias a uma empresa especialista nesta área faz-me sentir como um leigo a apresentar, a um medico cirurgião altamente capacitado, o que faria numa situação clinica em que não tem suficiente especialização e capacitação.
