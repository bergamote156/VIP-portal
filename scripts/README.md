# Scripts Frontend VIP Portal

Ce dossier contient les scripts bash pour gérer le frontend Vue.js du projet VIP Portal.

## Scripts disponibles

### 1. `build-frontend.sh` - Build de production
**Usage:** `./scripts/build-frontend.sh`

Ce script :
- Vérifie que Node.js et npm sont installés
- Nettoie le dossier `dist/` existant
- Installe les dépendances avec `npm ci`
- Lance le build de production avec `npm run build`
- Vérifie que l'index.html contient les chemins `/new_front/`
- Affiche des messages colorés pour le suivi

**Utilisé par Maven** lors de `mvn clean package`

### 2. `clean-frontend.sh` - Nettoyage
**Usage:** `./scripts/clean-frontend.sh`

Ce script :
- Supprime le dossier `frontend/dist/`
- Supprime le dossier `vip-portal/src/main/webapp/new_front/`
- Optionnel : supprime `node_modules/` (décommentez si nécessaire)

### 3. `dev-frontend.sh` - Serveur de développement
**Usage:** `./scripts/dev-frontend.sh`

Ce script :
- Lance le serveur de développement Vite
- Le site sera accessible sur `http://localhost:5173`
- Appuyez sur `Ctrl+C` pour arrêter

## Prérequis

### Installation sur Fedora
```bash
# Installer Node.js et npm
sudo dnf install nodejs npm

# Vérifier l'installation
node --version
npm --version
```

### Rendre les scripts exécutables
```bash
chmod +x scripts/*.sh
```

## Utilisation

### Développement
```bash
# Lancer le serveur de développement
./scripts/dev-frontend.sh
```

### Build manuel
```bash
# Builder le frontend manuellement
./scripts/build-frontend.sh
```

### Nettoyage
```bash
# Nettoyer tous les fichiers générés
./scripts/clean-frontend.sh
```

### Build Maven complet
```bash
# Build complet (Java + Frontend)
mvn clean package
```

## Structure des dossiers

```
VIP-portal/
├── frontend/                    # Code source Vue.js
│   ├── src/
│   ├── dist/                    # Build généré (ignoré par git)
│   └── package.json
├── vip-portal/
│   └── src/main/webapp/
│       └── new_front/           # Copié par Maven (ignoré par git)
└── scripts/
    ├── build-frontend.sh        # Script de build
    ├── clean-frontend.sh        # Script de nettoyage
    ├── dev-frontend.sh          # Script de développement
    └── README.md                # Ce fichier
```

## Configuration

### vite.config.js
Le fichier `frontend/vite.config.js` doit contenir :
```javascript
export default defineConfig({
  // ... autres configs
  base: '/new_front/', // Chemin de base pour le déploiement
  // ... autres configs
})
```

### pom.xml
Le POM exécute automatiquement `build-frontend.sh` lors de la phase `prepare-package`.

## Dépannage

### Erreur "Permission denied"
```bash
chmod +x scripts/*.sh
```

### Erreur "Node.js not found"
```bash
sudo dnf install nodejs npm
```

### Erreur "npm not found"
```bash
sudo dnf install npm
```

### Build échoue
1. Vérifiez que `vite.config.js` contient `base: '/new_front/'`
2. Vérifiez que tous les scripts sont exécutables
3. Vérifiez que Node.js et npm sont installés
4. Consultez les logs du script pour plus de détails

## Notes

- Les scripts utilisent `set -euo pipefail` pour une gestion d'erreur robuste
- Les messages sont colorés pour une meilleure lisibilité
- Les scripts vérifient automatiquement les prérequis
- Le build Maven est automatique et ne nécessite pas d'intervention manuelle
