# To do

- [ ] Desabilitar o botão enquanto está salvando na tela de bandeiras.
  coahmark ao entrar no app explicando as maquininhas
- dicas de como funciona a maquininha
- dicas para aparecer o coachmark na aba configuracoes caso ele precisa
- esqueci a senha
- titulo na home deve ser o nome do app
- Este App Bundle contém código nativo, e você não fez upload dos símbolos de depuração. Recomendamos que você faça upload de um arquivo de símbolo para facilitar a análise e a depuração de falhas e ANRs

# Pendências — LucroVarejo

## 1. Responsividade e dimensões

- [x] Ajustar as dimensões do aplicativo para dispositivos com telas pequenas.
- [x] Revisar o arquivo `dimens.xml`, priorizando o reaproveitamento dos valores que já existem.
- [x] Criar uma estratégia para adaptar os tamanhos sem precisar configurar manualmente cada componente.
- [x] Ajustar a tipografia globalmente, evitando precisar informar `fontSize` em cada `Text`.
- [x] Garantir que componentes que utilizam a tipografia padrão também respeitem a configuração.
- [x] Investigar componentes que estejam fora do tema ou utilizando estilos próprios e que não estejam respeitando a tipografia global.
- [x] Revisar especialmente a Navbar, verificando:
  - [x] Tamanho dos ícones.
  - [x] Tamanho dos textos.
  - [x] Espaçamentos.
  - [x] Altura da barra.
  - [x] Comportamento em dispositivos pequenos.

**Objetivo:** o aplicativo deve se adaptar a telas menores sem comprometer a legibilidade ou exigir ajustes individuais em cada componente.

## 2. Capitalização dos Inputs

- [x] Configurar todos os campos de entrada para iniciarem com a primeira letra maiúscula.
- [x] Aplicar a configuração globalmente, evitando repetir código em cada input.
- [x] Manter o campo de e-mail sem capitalização automática.
- [x] Verificar os campos de cadastro, edição e demais formulários.

**Regra:**
- Inputs comuns: primeira letra maiúscula.
- E-mail: sem capitalização automática.

## 3. Fechamento do teclado

- [ ] Implementar o fechamento do teclado ao clicar fora de qualquer input.
- [ ] Aplicar o comportamento de maneira global, evitando implementar individualmente em cada campo.
- [ ] Garantir que o campo perca o foco ao clicar fora.
- [ ] Verificar o comportamento em telas com vários inputs.
- [ ] Garantir que a interação com botões continue funcionando normalmente.
- [ ] Testar também em telas com rolagem (scroll).

**Objetivo:** o usuário não precisa procurar o botão de voltar do Android para fechar o teclado. Basta tocar em uma área fora do campo de entrada.

## 4. Validação final

- [ ] Testar em dispositivos com telas pequenas.
- [ ] Testar em dispositivos com telas maiores para garantir que nada fique desproporcional.
- [ ] Verificar todas as telas que possuem inputs.
- [ ] Verificar a Navbar em diferentes resoluções.
- [ ] Confirmar que os campos de e-mail continuam funcionando sem capitalização.
- [ ] Confirmar que nenhum texto ficou pequeno demais ou ilegível.
