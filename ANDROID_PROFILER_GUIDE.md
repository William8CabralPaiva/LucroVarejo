# Guia Completo do Android Studio Profiler

O **Android Studio Profiler** é a ferramenta oficial integrada ao Android Studio para análise de desempenho em tempo real e captura de rastreamentos (*traces*) do seu aplicativo. Com ele, é possível diagnosticar e otimizar o uso de **CPU**, **Memória**, **Rede (Network)** e **Energia/Bateria (Power/Battery)**.

---

## 1. Visão Geral e Telemetria em Tempo Real (*Live Telemetry*)

A seção de telemetria fornece um panorama instantâneo do comportamento do aplicativo enquanto ele está em execução. É a melhor opção para análises exploratórias.

### Componentes Principais
- **Interações (*Interactions*):** Exibe eventos do ciclo de vida da aplicação e ações do usuário (toques, rotações de tela, pressionamento de botões) ao longo do tempo.
- **Uso de CPU (*CPU Usage*):** Gráfico do percentual de uso de processador pelo seu aplicativo em relação à capacidade total do dispositivo e em comparação com outros processos do sistema.
- **Threads:** Lista todas as threads ativas do processo do app com códigos de cores indicativos:
  - 🟢 **Verde (*Running / Runnable*):** Thread ativa executando ou pronta para executar código na CPU.
  - 🟡 **Amarelo (*Waiting on I/O*):** Thread aguardando operações de Entrada/Saída (disco ou rede).
  - ⚪ **Cinza (*Sleeping*):** Thread em repouso/bloqueada aguardando um recurso ficar disponível.
- **Gráfico de Memória (*Memory Stacked Graph*):** Gráfico empilhado exibindo o consumo instantâneo por categoria de memória.

---

## 2. CPU Profiler (Análise de Processamento e Execução)

O **CPU Profiler** permite entender o tempo de execução de código, o comportamento das threads e identificar gargalos de renderização na interface.

### Rastreamento do Sistema (*System Trace*)
Registra a atividade global do sistema operacional e do aplicativo:
- **Display / Frames:** Examina a duração do ciclo de renderização na *Main Thread* e na *RenderThread* para detectar **UI Jank** (travamentos e perda de quadros).
- **CPU Cores:** Mostra a atividade individual de cada núcleo do processador (*cores* de alto desempenho vs. *cores* de eficiência energética na arquitetura *big.LITTLE*).

### Gravação e Amostragem de Métodos
- **Method Sampling (Amostragem):** Coleta a pilha de chamadas (*call stack*) em intervalos frequentes. Possui baixo *overhead* de desempenho e é ideal para identificar métodos lentos em execuções longas.
- **Method Tracing (Rastreamento):** Registra o momento exato de início e fim de cada função. Garante contagem exata de chamadas, mas possui maior impacto no desempenho durante a captura.

### Modos de Visualização
- **Flame Chart / Call Chart:** Visualizações gráficas em bloco da árvore de chamadas e profundidade de execução.
- **Top Down / Bottom Up:** Tabelas detalhadas para ordenar funções pelo tempo próprio (*Self Time*) ou tempo total com funções filhas (*Children + Self Time*).

---

## 3. Memory Profiler (Análise de Memória e Leaks)

Ajuda a evitar estouro de memória (**Out Of Memory - OOM**) e vazamentos de memória (**Memory Leaks**).

### Categorias de Memória
| Categoria | Descrição |
| :--- | :--- |
| **Java** | Memória alocada para objetos gerenciados pelo ecossistema Java/Kotlin. |
| **Native** | Memória alocada por código C/C++ ou buffers nativos (e.g. Bitmaps nativos). |
| **Graphics** | Recursos de renderização gráfica, superfícies GL/Vulkan e texturas. |
| **Code** | Memória ocupada pelo código compilado do app (DEX, código nativo e bibliotecas). |
| **Stack** | Memória utilizada pelas pilhas de chamadas de todas as threads ativas. |
| **Others** | Memória compartilhada com o sistema ou outros processos. |

### Principais Ferramentas
- **Capture Heap Dump:** Captura o estado atual da memória do app para inspecionar objetos ativos, instâncias duplicadas e a árvore de retenção (*retaining tree*).
- **Record Native Allocations:** Acompanha as alocações de memória efetuadas por bibliotecas ou código nativo em C/C++.

---

## 4. Network Profiler (Inspeção de Rede)

Monitora o tráfego de dados do aplicativo e inspeciona requisições HTTP/HTTPS em tempo real.

### Funcionalidades
- **Tráfego em Tempo Real:** Exibe a taxa de transferência de *upload* e *download* em KB/s e o número de conexões ativas.
- **Detalhamento de Requisições:**
  - **Headers:** Cabeçalhos enviados e recebidos.
  - **Payload / Response:** Corpo da requisição e da resposta (JSON, XML, imagens, etc.).
  - **Status Code:** Códigos de status HTTP (200, 400, 404, 500, etc.).
  - **Callstack:** A linha de código exata que originou a chamada de rede.

---

## 5. Power & Battery Profiler (Consumo de Energia)

Permite identificar quais partes do aplicativo causam alto consumo de bateria.

### Recursos
- **Power Rails:** Em dispositivos compatíveis (como celulares Google Pixel no Android Studio Hedgehog+), utiliza o *On Device Power Rails Monitor (ODPM)* para medir o consumo elétrico por subsistema:
  - **CPU**
  - **GPU**
  - **Display (Tela)**
  - **Modem / Celular (Rede móvel)**
  - **Wi-Fi**
- **Identificação de "Vampiros" de Bateria:** Ajuda a localizar chamadas excessivas de localização (GPS), alarmes contínuos (*WakeLocks*), ou falta de agrupamento em requisições de rede.

---

## 6. Dicas Úteis e Atalhos

### Configuração de Build
Para obter dados precisos de desempenho sem abrir mão de certas otimizações, inclua a tag `<profileable>` no seu `AndroidManifest.xml`:
```xml
<application ...>
    <profileable android:shell="true" />
</application>
```

### Navegação na Linha do Tempo
- **Zoom In / Zoom Out:** Teclas `W` / `S` ou `Ctrl + Scroll do mouse`.
- **Navegar Esquerda / Direita:** Teclas `A` / `D` ou segurar `Espaço` e arrastar o mouse.
