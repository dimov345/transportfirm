import { SidebarItem } from "./sidebar.model";

export const SIDEBAR_ITEMS: SidebarItem[] = [
  {
    label: 'Начална страница',
    icon: 'home',
    route: '/',
  },
  {
    label: 'Админ',
    icon: 'users',
    route: '/employees',
    roles: ['ADMIN', 'MANAGER']
  },
  {
    label: 'Екип и техникаn',
    icon: 'folder',
    children: [
      {
        label: 'Шофьори',
        icon: 'truck',
        route: '/drivers',
        roles: ['ADMIN', 'DISPATCHER']
      },
      {
        label: 'Спидитори',
        icon: 'lorry',
        route: '/dispatchers',
        roles: ['ADMIN', 'DISPATCHER']
      },
      {
        label: 'Механици',
        icon: 'wrench',
        route: '/mechanics',
        roles: ['ADMIN', 'MANAGER', 'MECHANIC']
      },
      {
        label: 'ППС',
        icon: 'lorry',
        route: '/vehicles',
        roles: ['ADMIN', 'DISPATCHER', 'MECHANIC']
      }
    ]
  },
  {
    label: 'Счетоводство',
    icon: 'chart',
    route: '/reports',
    roles: ['ADMIN', 'ACCOUNTANT']
  },
  {
    label: 'Профил',
    icon: 'settings',
    route: '/admin',
    roles: ['ADMIN']
  }
];
