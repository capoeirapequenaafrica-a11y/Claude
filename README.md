# Replica Studio 3D & Claude Copilot (Sem Limites)

> **App de Engenharia Reversa, Toolkit de 11 Skills do Replica, Claude Copilot e Visualizador 3D GLB/OBJ em Tempo Real para Android.**

---

## 🌟 Funcionalidades Principais

### 1. 🧊 3D Studio & GLB Engine
- **Visualizador 3D em Tempo Real (60 FPS)**: Rotação orbital em 360° (Pitch e Yaw), zoom e auto-rotação suave no Jetpack Compose.
- **Importador de Arquivos Customizados (File Picker)**: Abra qualquer arquivo `.glb` ou `.obj` local no seu dispositivo Android via ActivityResultContracts sem permissões intrusivas.
- **Carregador de URL Externa**: Insira links públicos para modelos `.glb` e visualize imediatamente no viewport.
- **Modos de Renderização**: Híbrido, Sombreado (iluminação direcional e Lambertian shading), Wireframe e Nuvem de Vértices.
- **Exportador**: Exportação com um clique para Wavefront `.OBJ` e inspeção de chunks binários `glTF 2.0 / GLB`.

### 2. ⚡ Claude Copilot (Modo Sem Limites)
- Assistente Claude configurado com toda a metodologia do Replica Skill.
- Alternador **Modo Sem Limites**: Autonomia total para gerar especificações arquiteturais, comandos limpos e sintetizar malhas 3D.
- Ações rápidas para executar `/replica-recon`, `/replica-architect`, `/replica-entrepreneur` e `/replica-deploy`.

### 3. 🛠️ As 11 Skills do Replica
1. `/replica-recon`: Engenharia reversa de telas, fluxos e modelos de dados.
2. `/replica-architect`: Blueprint de arquitetura, constraints e schema do banco.
3. `/replica-design`: Extração de design tokens e auditoria WCAG 2.2 contrast ratio.
4. `/replica-build`: Reconstrução tela a tela com estados de UI.
5. `/replica-backend`: Integrações, autenticação e webhooks.
6. `/replica-test`: Testes end-to-end e rastreamento de bugs por severidade (S1 a S4).
7. `/replica-diff`: Calculadora de paridade ponderada (Must: 3, Should: 2, Could: 1).
8. `/replica-entrepreneur`: Inteligência de avaliações, extração de dores e ângulo competitivo.
9. `/replica-brand`: Varredura clean-room anti-leaks de marcas concorrentes.
10. `/replica-launch`: Linter de limites e regras de metadados do Google Play e App Store.
11. `/replica-deploy`: Auditoria preflight de release de 6 pontos para certificação de lançamento.

---

## 🚀 Como Configurar e Enviar para o GitHub

Você pode organizar o projeto no GitHub de duas formas:

### Opção A: Repositório Único Completo (Recomendado)
Para publicar este aplicativo Android completo com o Claude Copilot e o motor 3D:

1. Crie um novo repositório no seu GitHub (ex: `replica-studio`):
   `https://github.com/capoeirapequenaafrica-a11y/replica-studio`

2. No terminal local (ou exportando o ZIP do AI Studio):
   ```bash
   git init
   git add .
   git commit -m "feat: Replica Studio 3D com Claude Copilot e suporte a GLB/OBJ"
   git branch -M main
   git remote add origin https://github.com/capoeirapequenaafrica-a11y/replica-studio.git
   git push -u origin main
   ```

3. O workflow do GitHub Actions em `.github/workflows/android.yml` compilará o APK e executará os testes unitários automaticamente a cada push!

---

### Opção B: Repositório Dedicado Exclusivo para o Claude (`replica-claude-agent`)
Se desejar manter um repositório isolado contendo apenas as especificações dos agentes, prompts e ferramentas de skill para o Claude (formato Claude Code / Anthropic Agent Skills):

1. Crie o repositório no GitHub:
   `https://github.com/capoeirapequenaafrica-a11y/claude-replica-skills`

2. Clone o repositório fork original já existente:
   ```bash
   git clone https://github.com/capoeirapequenaafrica-a11y/replica-skill.git
   cd replica-skill
   ```

3. Esse repositório contém as 11 pastas de skills prontas (`replica-recon/`, `replica-architect/`, `replica-diff/`, etc.) e os scripts Python padrão (`parity.py`, `contrast.py`, `reviews.py`, `sweep.py`, `listing.py`).

---

## 🏗️ Stack Tecnológica Android
- **Linguagem**: Kotlin 2.2
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Persistência**: Room Database com Kotlin Coroutines & Flow
- **Motor 3D**: Engine3D (vetores, normais, projeção 3D, parsing binário GLB e OBJ)
- **Networking**: OkHttp para download de modelos 3D externos
- **Testes**: Robolectric e JUnit 4

---

## 📄 Licença
Distribuído sob licença MIT. Livre para uso, modificação e publicação.
