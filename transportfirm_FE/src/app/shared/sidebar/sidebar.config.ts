import { SidebarItem } from "./sidebar.model";

export const SIDEBAR_ITEMS: SidebarItem[] = [
  {
    label: 'Начало',
    icon: '🏠',
    route: '/',
  },
  {
    label: 'Работници',
    icon: '👷‍♂️',
    route: '/employees',
    roles: ['ADMIN', 'MANAGER']
  },
  {
    label: 'Документи',
    icon: '📂',
    children: [
      {
        label: 'Шофьори',
        icon: '🚚',
        route: '/drivers',
        roles: ['ADMIN', 'MANAGER', 'DISPATCHER']
      },
      {
        label: 'ППС',
        icon: '🚛',
        route: '/vehicles',
        roles: ['ADMIN', 'MANAGER', 'MECHANIC']
      }
    ]
  },
  {
    label: 'Отчети',
    icon: '📊',
    route: '/reports',
    roles: ['ADMIN', 'ACCOUNTANT', 'MANAGER']
  },
  {
    label: 'Админ',
    icon: '⚙️',
    route: '/admin',
    roles: ['ADMIN']
  }
];
