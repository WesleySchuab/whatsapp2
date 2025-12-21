# Debug: Diferenciação de Mensagens

## Status Atual

O sistema está configurado para diferenciar mensagens automaticamente:

### ✅ Infraestrutura em Lugar
1. **Layouts diferentes**:
   - `itens_mensagens_remetente.xml` - Mensagens alinhadas à DIREITA (usuário logado)
   - `itens_mensagens_destinario.xml` - Mensagens alinhadas à ESQUERDA (outro usuário)

2. **Adapter com lógica de diferenciação**:
   - Compara `mensagem.idUsuario` com `FirebaseAuth.getInstance().currentUser?.uid`
   - Se forem iguais → `TIPO_REMETENTE` (direita)
   - Senão → `TIPO_DESTINATARIO` (esquerda)

3. **Logging detalhado** adicionado para debug:
   - Arquivo: `MensagensActivity.kt`
   - Arquivo: `MensagensAdapter.kt`

## Como Verificar se Está Funcionando

### 1️⃣ Abra o Logcat (Android Studio)
```
View → Tool Windows → Logcat
```

### 2️⃣ Busque por:
```
MensagensActivity
MensagensAdapter
```

### 3️⃣ Procure por estes logs:

#### Ao abrir a conversa:
```
MensagensActivity: === INICIANDO RECUPERAÇÃO DE DADOS ===
MensagensActivity: ID do usuário logado: <ID_AQUI>
MensagensActivity: ✓ Dados do remetente carregados: João (ID: <ID_AQUI>)
MensagensActivity: ✓ Dados do destinatário carregados: Maria (ID: <ID_DIFERENTE>)
MensagensActivity: === INICIANDO LISTENERS ===
MensagensActivity: Remetente (logado): <ID_AQUI>
MensagensActivity: Destinatário: <ID_DIFERENTE>
```

#### Ao carregar mensagens:
```
MensagensActivity: Mensagem ENVIADA carregada: idUsuario=<ID_AQUI>, msg='Oi'
MensagensActivity: Mensagem RECEBIDA carregada: idUsuario=<ID_DIFERENTE>, msg='Olá'
MensagensActivity: ✓ Mensagens ENVIADAS atualizadas: 1
MensagensActivity: ✓ Mensagens RECEBIDAS atualizadas: 1
```

#### Ao exibir na RecyclerView:
```
MensagensAdapter: adicionarLista() chamado com 2 mensagens. ID logado: <ID_AQUI>
MensagensAdapter: [0] idUsuario=<ID_AQUI>, mensagem='Oi'
MensagensAdapter: [1] idUsuario=<ID_DIFERENTE>, mensagem='Olá'
MensagensAdapter: getItemViewType[0]: idUsuario=<ID_AQUI>, idLogado=<ID_AQUI>, tipo=0
MensagensAdapter: getItemViewType[1]: idUsuario=<ID_DIFERENTE>, idLogado=<ID_AQUI>, tipo=1
MensagensAdapter: Criado MensagensRemetenteViewHolder
MensagensAdapter: Criado MensagensDestinatarioViewHolder
```

## Possíveis Problemas

### ❌ Todas as mensagens aparecem do lado esquerdo
**Causa**: `idUsuario` está vazio ou inválido nas mensagens salvas
**Solução**: Verificar se `Mensagem.idUsuario` está sendo preenchido em `salvarMensagem()`

### ❌ IDs logado e destinatário são iguais
**Causa**: Erro ao recuperar destinatário da Intent
**Solução**: Verificar se `ContatosFragment` está passando `dadosDestinatario` corretamente

### ❌ Nenhuma mensagem aparece
**Causa**: Listeners não foram inicializados ou IDs nulos
**Solução**: Procurar por `❌ IDs não disponíveis` no Logcat

## Estrutura de Dados no Firebase

```
usuarios/
  {userId}/
    nome: "João"
    id: {userId}
    ...

mensagens/
  {remetente_id}/
    {destinatario_id}/
      {messageId}/
        idUsuario: {remetente_id}  ← Importante para diferenciação
        mensagem: "Olá"
        data: 2025-12-21...
```

## Teste Prático

1. Abra a conversa entre dois usuários
2. Envie uma mensagem como Usuário A
3. Volte e abra a conversa como Usuário B
4. Verifique o Logcat para confirmar que:
   - `idUsuario` de mensagens enviadas por A = ID de A
   - `idUsuario` de mensagens recebidas por B = ID de A
   - O adapter diferencia corretamente (tipo 0 vs 1)
