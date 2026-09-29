import React, { useState } from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import Modal from './Modal';

describe('Modal component', () => {
  beforeEach(() => {
    document.body.style.overflow = '';
  });

  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  it('não renderiza nada quando isOpen é false', () => {
    render(
      <Modal isOpen={false} onClose={vi.fn()} title="Modal Fechado">
        <p>Conteúdo</p>
      </Modal>
    );

    expect(screen.queryByRole('dialog')).toBeNull();
    expect(screen.queryByText('Modal Fechado')).toBeNull();
  });

  it('renderiza com role="dialog", aria-modal="true" e aria-labelledby associado ao título', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()} title="Título do Diálogo">
        <p>Conteúdo do Diálogo</p>
      </Modal>
    );

    const dialog = screen.getByRole('dialog');
    expect(dialog).toBeDefined();
    expect(dialog.getAttribute('aria-modal')).toBe('true');

    const title = screen.getByRole('heading', { level: 2, name: 'Título do Diálogo' });
    expect(title).toBeDefined();
    expect(dialog.getAttribute('aria-labelledby')).toBe(title.id);
  });

  it('suporta atributo opcional aria-describedby', () => {
    render(
      <Modal
        isOpen={true}
        onClose={vi.fn()}
        title="Título"
        ariaDescribedBy="modal-description"
      >
        <p id="modal-description">Texto explicativo acessível</p>
      </Modal>
    );

    const dialog = screen.getByRole('dialog');
    expect(dialog).toBeDefined();
    expect(dialog.getAttribute('aria-describedby')).toBe('modal-description');
  });

  it('posiciona o foco inicial no elemento com autoFocus', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()} title="Teste Foco Inicial">
        <div>
          <button type="button">Primeiro Botão</button>
          <input data-testid="autofocus-input" autoFocus placeholder="Nome" />
          <button type="button">Terceiro Botão</button>
        </div>
      </Modal>
    );

    const input = screen.getByTestId('autofocus-input');
    expect(document.activeElement).toBe(input);
  });

  it('posiciona o foco no primeiro elemento interativo do conteúdo se não houver autoFocus', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()} title="Teste Sem AutoFocus">
        <div>
          <input data-testid="first-input" placeholder="Primeiro Campo" />
          <button type="button">Botão</button>
        </div>
      </Modal>
    );

    const input = screen.getByTestId('first-input');
    expect(document.activeElement).toBe(input);
  });

  it('confinamento de foco (Focus Trap): cicla do último para o primeiro elemento com Tab', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()} title="Focus Trap Tab">
        <div>
          <input data-testid="campo-1" />
          <button data-testid="botao-salvar">Salvar</button>
        </div>
      </Modal>
    );

    const closeBtn = screen.getByRole('button', { name: /fechar modal|close modal/i });
    const salvarBtn = screen.getByTestId('botao-salvar');

    // Foca o último elemento do modal (botao-salvar)
    salvarBtn.focus();
    expect(document.activeElement).toBe(salvarBtn);

    // Pressiona Tab no último elemento
    fireEvent.keyDown(salvarBtn, { key: 'Tab' });

    // Deve ciclar para o primeiro elemento focável do modal (closeBtn)
    expect(document.activeElement).toBe(closeBtn);
  });

  it('confinamento de foco (Focus Trap): cicla do primeiro para o último elemento com Shift+Tab', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()} title="Focus Trap Shift+Tab">
        <div>
          <input data-testid="campo-1" />
          <button data-testid="botao-salvar">Salvar</button>
        </div>
      </Modal>
    );

    const closeBtn = screen.getByRole('button', { name: /fechar modal|close modal/i });
    const salvarBtn = screen.getByTestId('botao-salvar');

    // Foca o primeiro elemento do modal (closeBtn)
    closeBtn.focus();
    expect(document.activeElement).toBe(closeBtn);

    // Pressiona Shift+Tab no primeiro elemento
    fireEvent.keyDown(closeBtn, { key: 'Tab', shiftKey: true });

    // Deve ciclar para o último elemento focável do modal (salvarBtn)
    expect(document.activeElement).toBe(salvarBtn);
  });

  it('fecha o modal ao pressionar a tecla Escape quando disableClose não estiver ativo', () => {
    const handleClose = vi.fn();
    render(
      <Modal isOpen={true} onClose={handleClose} title="Escape Test">
        <p>Conteúdo</p>
      </Modal>
    );

    fireEvent.keyDown(document, { key: 'Escape' });
    expect(handleClose).toHaveBeenCalledTimes(1);
  });

  it('não fecha o modal ao pressionar Escape quando disableClose for true', () => {
    const handleClose = vi.fn();
    render(
      <Modal isOpen={true} onClose={handleClose} title="Disable Close Test" disableClose={true}>
        <p>Conteúdo bloqueado</p>
      </Modal>
    );

    fireEvent.keyDown(document, { key: 'Escape' });
    expect(handleClose).not.toHaveBeenCalled();
  });

  it('restaura o foco para o elemento acionador original ao fechar o modal', () => {
    const TestTriggerWrapper = () => {
      const [isOpen, setIsOpen] = useState(false);
      return (
        <div>
          <button data-testid="open-modal-trigger" onClick={() => setIsOpen(true)}>
            Abrir Modal
          </button>
          <Modal isOpen={isOpen} onClose={() => setIsOpen(false)} title="Modal Teste">
            <button data-testid="close-internal-btn" onClick={() => setIsOpen(false)}>
              Fechar Interno
            </button>
          </Modal>
        </div>
      );
    };

    render(<TestTriggerWrapper />);

    const triggerBtn = screen.getByTestId('open-modal-trigger');
    triggerBtn.focus();
    expect(document.activeElement).toBe(triggerBtn);

    // Abre o modal
    fireEvent.click(triggerBtn);
    expect(screen.getByRole('dialog')).toBeDefined();

    // Fecha o modal via botão interno
    const closeBtn = screen.getByTestId('close-internal-btn');
    fireEvent.click(closeBtn);

    // Modal fechado, foco deve ter voltado para o gatilho original
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(document.activeElement).toBe(triggerBtn);
  });

  it('bloqueia o scroll de fundo ao abrir e restaura ao fechar', () => {
    const { rerender } = render(
      <Modal isOpen={true} onClose={vi.fn()} title="Scroll Lock Test">
        <p>Conteúdo</p>
      </Modal>
    );

    expect(document.body.style.overflow).toBe('hidden');

    rerender(
      <Modal isOpen={false} onClose={vi.fn()} title="Scroll Lock Test">
        <p>Conteúdo</p>
      </Modal>
    );

    expect(document.body.style.overflow).toBe('');
  });

  it('respeita initialFocusRef quando informado', () => {
    const TestComponent = () => {
      const secondInputRef = React.useRef<HTMLInputElement>(null);
      return (
        <Modal
          isOpen={true}
          onClose={vi.fn()}
          title="Initial Focus Ref"
          initialFocusRef={secondInputRef}
        >
          <div>
            <input data-testid="input-1" placeholder="Input 1" />
            <input ref={secondInputRef} data-testid="input-2" placeholder="Input 2" />
          </div>
        </Modal>
      );
    };

    render(<TestComponent />);
    const input2 = screen.getByTestId('input-2');
    expect(document.activeElement).toBe(input2);
  });

  it('não sequestra o foco no Tab quando o foco estiver em outro role="dialog" aninhado', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()} title="Modal Pai">
        <div>
          <button data-testid="btn-modal">Botão Modal</button>
          <div role="dialog" aria-label="Popover Aninhado">
            <button data-testid="btn-popover-1">Popover 1</button>
            <button data-testid="btn-popover-2">Popover 2</button>
          </div>
        </div>
      </Modal>
    );

    const btnPopover1 = screen.getByTestId('btn-popover-1');
    btnPopover1.focus();
    expect(document.activeElement).toBe(btnPopover1);

    // Pressiona Tab enquanto está no popover aninhado
    const event = new KeyboardEvent('keydown', { key: 'Tab', bubbles: true, cancelable: true });
    document.dispatchEvent(event);

    // O modal pai não deve prevenir o evento nem alterar o activeElement
    expect(event.defaultPrevented).toBe(false);
  });

  it('não fecha o modal pai no Escape quando o foco estiver em outro role="dialog" aninhado', () => {
    const handleClose = vi.fn();
    render(
      <Modal isOpen={true} onClose={handleClose} title="Modal Pai">
        <div>
          <button data-testid="btn-modal">Botão Modal</button>
          <div role="dialog" aria-label="DatePicker Aninhado">
            <button data-testid="btn-picker">Dia 15</button>
          </div>
        </div>
      </Modal>
    );

    const btnPicker = screen.getByTestId('btn-picker');
    btnPicker.focus();
    expect(document.activeElement).toBe(btnPicker);

    fireEvent.keyDown(btnPicker, { key: 'Escape', bubbles: true });

    expect(handleClose).not.toHaveBeenCalled();
  });

  it('ignora elementos com tabindex="-1" no Focus Trap cíclico', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()} title="Modal TabIndex -1">
        <div>
          <button data-testid="btn-focavel-1">Focável 1</button>
          <button data-testid="btn-focavel-2">Focável 2</button>
          <button data-testid="btn-ignorado" tabIndex={-1}>Ignorado</button>
        </div>
      </Modal>
    );

    const btn2 = screen.getByTestId('btn-focavel-2');
    const closeBtn = screen.getByRole('button', { name: /fechar modal|close modal/i });

    // Como btn-ignorado tem tabIndex={-1}, o último elemento focável é btn2
    btn2.focus();
    expect(document.activeElement).toBe(btn2);

    // Tab no último elemento cicla para o primeiro (closeBtn)
    fireEvent.keyDown(btn2, { key: 'Tab' });
    expect(document.activeElement).toBe(closeBtn);

    // Shift+Tab no primeiro elemento cicla para o último focável (btn2)
    fireEvent.keyDown(closeBtn, { key: 'Tab', shiftKey: true });
    expect(document.activeElement).toBe(btn2);
  });
});
