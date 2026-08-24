import { describe, expectTypeOf, it } from 'vitest'
import type { Application, ApplicationImportPayload, PrecisePage, VipGroup } from '@/types/application.types'
import type { AppVersion, BoutiquesDescriptor } from '@/types/appversion.types'
import type { LoginCredentials, VipSession, VipUserLevel } from '@/types/auth.types'
import type { DashboardNotification, NotificationType } from '@/types/notification.types'

describe('types contracts', () => {
  it('validates application-related type shapes', () => {
    const group: VipGroup = {
      name: 'Neuro',
      publicGroup: true,
      type: 'team',
      auto: false,
    }

    const app: Application = {
      name: 'freesurfer',
      fullName: 'FreeSurfer',
      citation: null,
      owner: null,
      groups: [group],
      note: 'Pipeline',
    }

    const page: PrecisePage<Application> = {
      data: [app],
      total: 1,
    }

    const payload: ApplicationImportPayload = {
      executionResource: 'node-a',
      existingTag: 'stable',
    }

    expectTypeOf(page).toMatchTypeOf<PrecisePage<Application>>()
    expectTypeOf(payload.jsonFile).toEqualTypeOf<File | undefined>()

    // @ts-expect-error `groups` must be a VipGroup[]
    const invalidApp: Application = { ...app, groups: ['not-a-group'] }
    void invalidApp
  })

  it('validates auth and notification unions', () => {
    const credentials: LoginCredentials = {
      username: 'user@example.com',
      password: 'secret',
    }

    const level: VipUserLevel = 'User'
    const session: VipSession = {
      id: 'session-1',
      email: credentials.username,
      userlevel: level,
    }

    const notificationType: NotificationType = 'success'
    const dashboardNotification: DashboardNotification = {
      id: 'n-1',
      type: 'execution_completed',
      title: 'Done',
      description: 'Execution completed successfully',
      date: '2026-03-24',
      read: false,
    }

    expectTypeOf(session.userlevel).toEqualTypeOf<VipUserLevel>()
    expectTypeOf(notificationType).toBeString()
    expectTypeOf(dashboardNotification.link).toEqualTypeOf<string | undefined>()

    // @ts-expect-error invalid user level literal
    const badLevel: VipUserLevel = 'SuperAdmin'
    void badLevel

    // @ts-expect-error invalid dashboard notification type literal
    const badDashboardType: DashboardNotification['type'] = 'other'
    void badDashboardType
  })

  it('validates app version descriptor shape', () => {
    const descriptor: BoutiquesDescriptor = {
      name: 'Demo tool',
      version: '1.0.0',
      inputs: [{ id: 'in-1', type: 'String' }],
    }

    const version: AppVersion = {
      applicationName: 'demo-app',
      version: '1.0.0',
      descriptor: JSON.stringify(descriptor),
      parsedDescriptor: descriptor,
      doi: null,
      visible: true,
      resources: [],
      tags: [
        {
          key: 'stable',
          value: 'true',
          type: 'BOOLEAN',
          application: 'demo-app',
          version: '1.0.0',
          visible: true,
          boutiques: false,
        },
      ],
      settings: [],
      source: null,
      note: null,
    }

    expectTypeOf(version.parsedDescriptor).toEqualTypeOf<BoutiquesDescriptor | null>()
    expectTypeOf(version.visible).toEqualTypeOf<boolean>()

    // @ts-expect-error `visible` must be boolean
    const badVersion: AppVersion = { ...version, visible: 'yes' }
    void badVersion
  })
})
