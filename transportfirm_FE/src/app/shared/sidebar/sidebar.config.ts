import { SidebarItem } from "./sidebar.model";

export const SIDEBAR_ITEMS: SidebarItem[] = [
  {
    label: 'Начало',
    icon: 'home',
    route: '/',
  },
  {
    label: 'Работници',
    icon: 'users',
    route: '/employees',
    roles: ['ADMIN', 'MANAGER']
  },
  {
    label: 'Документи',
    icon: 'folder',
    children: [
      {
        label: 'Шофьори',
        icon: 'truck',
        route: '/drivers',
        roles: ['ADMIN', 'MANAGER', 'DISPATCHER']
      },
      {
        label: 'ППС',
        icon: 'lorry',
        route: '/vehicles',
        roles: ['ADMIN', 'MANAGER', 'MECHANIC']
      }
    ]
  },
  {
    label: 'Отчети',
    icon: 'chart',
    route: '/reports',
    roles: ['ADMIN', 'ACCOUNTANT', 'MANAGER']
  },
  {
    label: 'Админ',
    icon: 'settings',
    route: '/admin',
    roles: ['ADMIN']
  }
];
