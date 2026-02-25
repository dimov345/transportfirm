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
    label: 'Екип и техника',
    icon: 'folder',
    children: [
      {
        label: 'Шофьори',
        icon: 'truck',
        route: '/drivers',
        roles: ['ADMIN', 'MANAGER', 'DISPATCHER']
      },
      {
        label: 'Спедитори',
        icon: 'lorry',
        route: '/dispatchers',
        roles: ['ADMIN', 'MANAGER']
      },
      {
        label: 'Механици',
        icon: 'wrench',
        route: '/mechanics',
        roles: ['ADMIN', 'MANAGER']
      },
      {
        label: 'Моите ППС',
        icon: 'wrench',
        route: '/mechanic',
        roles: ['MECHANIC']
      },
      {
        label: 'Моите ППС',
        icon: 'lorry',
        route: '/dispatcher',
        roles: ['DISPATCHER']
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
    route: '/profile'
  }
];
