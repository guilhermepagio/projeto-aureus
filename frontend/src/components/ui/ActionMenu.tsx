import React, { useState, useRef, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { Menu, Pencil, Trash2 } from 'lucide-react';

interface ActionMenuProps {
  onEdit: () => void;
  onDelete: () => void;
}

const ActionMenu: React.FC<ActionMenuProps> = ({ onEdit, onDelete }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [coords, setCoords] = useState({ top: 0, left: 0 });
  const [isEven, setIsEven] = useState(false);
  const buttonRef = useRef<HTMLButtonElement>(null);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as Node;
      if (
        menuRef.current && !menuRef.current.contains(target) &&
        buttonRef.current && !buttonRef.current.contains(target)
      ) {
        setIsOpen(false);
      }
    };
    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isOpen]);

  const toggleMenu = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (buttonRef.current && !isOpen) {
      const rect = buttonRef.current.getBoundingClientRect();
      const even = buttonRef.current.closest('tr')?.matches(':nth-child(even)') || false;
      setIsEven(even);
      setCoords({
        top: rect.bottom + window.scrollY - 8,
        left: rect.left + window.scrollX - 120
      });
    }
    setIsOpen(!isOpen);
  };

  return (
    <>
      <button
        ref={buttonRef}
        type="button"
        onClick={toggleMenu}
        className="flex items-center justify-center p-1 rounded-md text-gray-500 hover:text-gray-900 hover:bg-black/10 transition-colors cursor-pointer"
        title="Ações"
      >
        <Menu className="w-4 h-4" />
      </button>

      {isOpen && createPortal(
        <div
          ref={menuRef}
          className={`absolute z-[9999] w-32 rounded-md shadow-lg ring-1 ring-black/5 focus:outline-none ${isEven ? 'bg-gray-200' : 'bg-white'}`}
          style={{ top: coords.top, left: coords.left }}
        >
          <div className="py-1">
            <button
              onClick={(e) => {
                e.stopPropagation();
                setIsOpen(false);
                onEdit();
              }}
              className="flex w-full items-center px-4 py-2 text-sm text-gray-700 hover:bg-black/5 cursor-pointer"
            >
              <Pencil className="w-4 h-4 mr-2" />
              Editar
            </button>
            <button
              onClick={(e) => {
                e.stopPropagation();
                setIsOpen(false);
                onDelete();
              }}
              className="flex w-full items-center px-4 py-2 text-sm text-red-600 hover:bg-red-500/10 cursor-pointer"
            >
              <Trash2 className="w-4 h-4 mr-2" />
              Excluir
            </button>
          </div>
        </div>,
        document.body
      )}
    </>
  );
};

export default ActionMenu;
