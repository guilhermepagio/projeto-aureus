import { create } from 'zustand';

interface MonthState {
  selectedMonth: string; // Format: 'YYYY-MM'
  isGlobalFilterActive: boolean;
  setSelectedMonth: (month: string) => void;
  toggleGlobalFilter: () => void;
  setGlobalFilter: (isActive: boolean) => void;
}

const now = new Date();
const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;

export const useMonthStore = create<MonthState>((set) => ({
  selectedMonth: currentMonth,
  isGlobalFilterActive: false,
  setSelectedMonth: (month) => set({ selectedMonth: month }),
  toggleGlobalFilter: () => set((state) => ({ isGlobalFilterActive: !state.isGlobalFilterActive })),
  setGlobalFilter: (isActive) => set({ isGlobalFilterActive: isActive }),
}));
