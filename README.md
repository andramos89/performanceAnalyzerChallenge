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
