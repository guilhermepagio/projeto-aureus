import React, { useEffect, useRef, useId } from 'react';
import { createPortal } from 'react-dom';

export interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
  disableClose?: boolean;
  maxWidth?: string;
  ariaDescribedBy?: string;
  'aria-describedby'?: string;
  ariaLabelledBy?: string;
  'aria-labelledby'?: string;
  initialFocusRef?: React.RefObject<HTMLElement | null>;
}

const FOCUSABLE_SELECTORS = [
  'a[href]',
  'button:not([disabled])',
  'input:not([disabled]):not([type="hidden"])',
  'select:not([disabled])',
  'textarea:not([disabled])',
  '[tabindex]:not([tabindex="-1"])',
].join(', ');

const getFocusableElements = (container: HTMLElement): HTMLElement[] => {
  const elements = container.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTORS);
  return Array.from(elements).filter((el) => {
    if (el.hasAttribute('disabled') || el.getAttribute('aria-hidden') === 'true') {
      return false;
    }
    if (el.getAttribute('tabindex') === '-1' || el.tabIndex === -1) {
      return false;
    }
    if (el.closest('fieldset[disabled]')) {
      return false;
    }
    if (typeof window !== 'undefined' && window.getComputedStyle) {
      try {
        const style = window.getComputedStyle(el);
        if (style.display === 'none' || style.visibility === 'hidden') {
          return false;
        }
      } catch {
        // ignore in environments without getComputedStyle support
      }
    }
    return true;
  });
};

const hasAutoFocus = (el: HTMLElement): boolean => {
  if (
    (el as HTMLInputElement).autofocus ||
    el.hasAttribute('autofocus') ||
    el.hasAttribute('data-autofocus')
  ) {
    return true;
  }
  const reactPropKey = Object.keys(el).find((k) => k.startsWith('__reactProps'));
  if (reactPropKey && (el as any)[reactPropKey]?.autoFocus) {
    return true;
  }
  return false;
};

const Modal: React.FC<ModalProps> = ({
  isOpen,
  onClose,
  title,
  children,
  disableClose,
  maxWidth = 'max-w-md',
  ariaDescribedBy,
  'aria-describedby': ariaDescribedbyKebab,
  ariaLabelledBy,
  'aria-labelledby': ariaLabelledbyKebab,
  initialFocusRef,
}) => {
  const modalRef = useRef<HTMLDivElement>(null);
  const previousActiveElement = useRef<HTMLElement | null>(null);
  const disableCloseRef = useRef(disableClose);
  disableCloseRef.current = disableClose;
  const onCloseRef = useRef(onClose);
  onCloseRef.current = onClose;

  const generatedId = useId();
  const titleId = ariaLabelledbyKebab || ariaLabelledBy || `modal-title-${generatedId.replace(/:/g, '')}`;
  const describedById = ariaDescribedbyKebab || ariaDescribedBy;

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    // Capture the trigger element before moving focus
    previousActiveElement.current = (document.activeElement as HTMLElement) || null;

    const originalOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';

    // Set initial focus
    if (initialFocusRef?.current) {
      initialFocusRef.current.focus();
    } else if (modalRef.current) {
      const allElements = Array.from(modalRef.current.querySelectorAll<HTMLElement>('*'));
      const autoFocusEl = allElements.find(hasAutoFocus);

      if (autoFocusEl) {
        autoFocusEl.focus();
      } else {
        const contentArea = modalRef.current.querySelector<HTMLElement>('[data-modal-content], .overflow-y-auto');
        const contentFocusables = contentArea ? getFocusableElements(contentArea) : [];
        if (contentFocusables.length > 0) {
          contentFocusables[0].focus();
        } else {
          const focusables = getFocusableElements(modalRef.current);
          if (focusables.length > 0) {
            focusables[0].focus();
          } else {
            modalRef.current.focus();
          }
        }
      }
    }

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        if (!modalRef.current) return;

        // Se o elemento ativo estiver dentro de outro role="dialog" diferente do nosso modal (ex: popover de date/month picker), não fecha o modal
        const activeDialog = (document.activeElement as HTMLElement | null)?.closest('[role="dialog"]');
        if (activeDialog && activeDialog !== modalRef.current) {
          return;
        }

        if (!disableCloseRef.current) {
          e.preventDefault();
          onCloseRef.current();
        }
        return;
      }

      if (e.key === 'Tab') {
        if (!modalRef.current) return;

        // Se o elemento ativo estiver dentro de outro role="dialog" diferente do nosso modal (ex: popover aninhado em portal), não rouba o foco
        const activeDialog = (document.activeElement as HTMLElement | null)?.closest('[role="dialog"]');
        if (activeDialog && activeDialog !== modalRef.current) {
          return;
        }

        const focusables = getFocusableElements(modalRef.current);
        if (focusables.length === 0) {
          e.preventDefault();
          return;
        }

        const firstElement = focusables[0];
        const lastElement = focusables[focusables.length - 1];

        if (e.shiftKey) {
          // Shift + Tab: se estiver no primeiro elemento, no container do modal ou fora dele, vai para o último
          if (
            document.activeElement === firstElement ||
            document.activeElement === modalRef.current ||
            !modalRef.current.contains(document.activeElement)
          ) {
            e.preventDefault();
            lastElement.focus();
          }
        } else {
          // Tab: se estiver no último elemento, no container do modal ou fora dele, vai para o primeiro
          if (
            document.activeElement === lastElement ||
            document.activeElement === modalRef.current ||
            !modalRef.current.contains(document.activeElement)
          ) {
            e.preventDefault();
            firstElement.focus();
          }
        }
      }
    };

    document.addEventListener('keydown', handleKeyDown);

    return () => {
      document.removeEventListener('keydown', handleKeyDown);
      document.body.style.overflow = originalOverflow;
      if (
        previousActiveElement.current &&
        typeof previousActiveElement.current.focus === 'function' &&
        document.contains(previousActiveElement.current)
      ) {
        previousActiveElement.current.focus();
      }
      previousActiveElement.current = null;
    };
  }, [isOpen, initialFocusRef]);

  if (!isOpen) return null;

  return createPortal(
    <div className="fixed inset-0 z-[9999] flex items-center justify-center p-4 bg-black/40">
      <div
        ref={modalRef}
        tabIndex={-1}
        className={`bg-white rounded-lg shadow-2xl drop-shadow-2xl w-full ${maxWidth} overflow-hidden flex flex-col focus:outline-none`}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        {...(describedById ? { 'aria-describedby': describedById } : {})}
      >
        <div className="flex justify-between items-center p-4 border-b">
          <h2 id={titleId} className="text-lg font-semibold text-gray-800">
            {title}
          </h2>
          <button
            type="button"
            onClick={onClose}
            disabled={disableClose}
            className={`cursor-pointer rounded p-1 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 ${
              disableClose ? 'text-gray-300 cursor-not-allowed' : 'text-gray-500 hover:text-gray-700'
            }`}
            aria-label="Fechar modal"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        <div className="p-4 overflow-y-auto" data-modal-content>
          {children}
        </div>
      </div>
    </div>,
    document.body
  );
};

export default Modal;
