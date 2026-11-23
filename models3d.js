// Procedural Three.js PBR 3D Model Library — All 11 Mini Militia Outpost Assets
window.buildMiniMilitiaModels = function (THREE) {
  function mat(color, metalness = 0.1, roughness = 0.65, emissive = 0x000000, doubleSided = false) {
    return new THREE.MeshStandardMaterial({
      color,
      metalness,
      roughness,
      emissive,
      side: doubleSided ? THREE.DoubleSide : THREE.FrontSide
    });
  }

  function addPart(group, geo, material, pos = [0, 0, 0], rot = [0, 0, 0], scale = [1, 1, 1]) {
    const m = new THREE.Mesh(geo, material);
    m.position.set(pos[0], pos[1], pos[2]);
    m.rotation.set(rot[0], rot[1], rot[2]);
    m.scale.set(scale[0], scale[1], scale[2]);
    m.castShadow = true;
    m.receiveShadow = true;
    group.add(m);
    return m;
  }

  function buildSoldier(variant) {
    const g = new THREE.Group();
    const mSkin = mat(0xcc8f61, 0.0, 0.55);
    const mBeard = mat(0x382414, 0.0, 0.85);
    const mWhite = mat(0xf2f2f2, 0.0, 0.25);
    const mPupil = mat(0x1f242e, 0.1, 0.15);
    const mBlack = mat(0x1a1c1f, 0.25, 0.45);
    const mLeather = mat(0x332114, 0.05, 0.6);
    const mBrass = mat(0xd9ad40, 0.85, 0.25);
    const mSteel = mat(0xa6adb8, 0.85, 0.3);
    const mGlow = mat(0xff9e2e, 0.0, 0.3, 0xe6731a);

    const mHelmet = variant === "noobie" ? mat(0x2e6124, 0.15, 0.35)
      : variant === "sarge" ? mat(0x5c4529, 0.0, 0.75) : mat(0x29303b, 0.25, 0.4);
    const mJacket = variant === "noobie" ? mat(0x385c2e, 0.0, 0.78)
      : variant === "sarge" ? mat(0xa38a57, 0.0, 0.75) : mat(0x2e333d, 0.05, 0.72);
    const mPants = variant === "noobie" ? mat(0x243d1f, 0.0, 0.82)
      : variant === "sarge" ? mat(0x8a7347, 0.0, 0.8) : mat(0x1f242e, 0.05, 0.78);
    const mAccent = variant === "noobie" ? mat(0x4d572e, 0.0, 0.8)
      : variant === "sarge" ? mat(0x265229, 0.0, 0.7) : mat(0x1a1f26, 0.1, 0.65);

    // Chibi Head
    addPart(g, new THREE.SphereGeometry(0.45, 20, 14), mSkin, [0, 1.38, 0], [0, 0, 0], [1.02, 0.98, 0.98]);
    for (const sx of [-1, 1]) {
      addPart(g, new THREE.SphereGeometry(0.08, 10, 8), mSkin, [sx * 0.45, 1.35, -0.02]);
    }

    if (variant === "sarge") {
      const mShades = mat(0x0d0f14, 0.85, 0.12);
      for (const sx of [-1, 1]) {
        addPart(g, new THREE.SphereGeometry(0.12, 12, 10), mShades, [sx * 0.16, 1.40, 0.41], [0, 0, 0], [1.1, 0.8, 0.4]);
      }
      addPart(g, new THREE.BoxGeometry(0.10, 0.03, 0.04), mBrass, [0, 1.42, 0.43]);
    } else {
      for (const sx of [-1, 1]) {
        addPart(g, new THREE.SphereGeometry(0.09, 12, 8), mWhite, [sx * 0.15, 1.40, 0.40], [0, 0, 0], [1, 0.85, 0.5]);
        addPart(g, new THREE.SphereGeometry(0.045, 10, 8), mPupil, [sx * 0.15, 1.40, 0.44]);
        addPart(g, new THREE.BoxGeometry(0.19, 0.06, 0.06), mBeard, [sx * 0.16, 1.50, 0.40], [0, 0, -sx * 0.22]);
      }
    }

    if (variant === "noobie" || variant === "sarge") {
      addPart(g, new THREE.SphereGeometry(0.47, 18, 12, 0, Math.PI * 2, Math.PI * 0.48, Math.PI * 0.52), mBeard, [0, 1.37, 0.01], [0, 0, 0], [1, 0.68, 0.96]);
    }

    // Gritting teeth
    addPart(g, new THREE.BoxGeometry(0.24, 0.09, 0.06), mWhite, [0, 1.21, 0.415]);
    addPart(g, new THREE.BoxGeometry(0.245, 0.014, 0.064), mBlack, [0, 1.21, 0.417]);

    // Headgear
    if (variant === "sarge") {
      addPart(g, new THREE.CylinderGeometry(0.68, 0.70, 0.05, 22), mHelmet, [0, 1.66, 0], [-0.06, 0, 0]);
      addPart(g, new THREE.CylinderGeometry(0.22, 0.44, 0.34, 20), mHelmet, [0, 1.84, 0], [-0.06, 0, 0]);
      addPart(g, new THREE.CylinderGeometry(0.45, 0.45, 0.07, 20), mBlack, [0, 1.71, 0], [-0.06, 0, 0]);
      addPart(g, new THREE.CylinderGeometry(0.09, 0.09, 0.04, 14), mBrass, [0, 1.80, 0.35], [Math.PI / 2, 0, 0]);
    } else {
      addPart(g, new THREE.SphereGeometry(0.51, 22, 14, 0, Math.PI * 2, 0, Math.PI * 0.56), mHelmet, [0, 1.44, -0.01], [0, 0, 0], [1, 0.88, 1]);
      addPart(g, new THREE.TorusGeometry(0.50, 0.035, 8, 24), mHelmet, [0, 1.40, -0.01], [Math.PI / 2, 0, 0]);
    }

    // Torso
    addPart(g, new THREE.CylinderGeometry(0.32, 0.36, 0.64, 18), mJacket, [0, 0.72, 0]);
    addPart(g, new THREE.SphereGeometry(0.33, 16, 10), mJacket, [0, 1.02, 0], [0, 0, 0], [1, 0.42, 0.96]);

    if (variant === "noobie") {
      addPart(g, new THREE.TorusGeometry(0.38, 0.035, 8, 22), mLeather, [0, 0.74, 0], [0.1, 0.2, 0.72], [1, 1.32, 0.92]);
      for (let i = 0; i < 8; i++) {
        const t = -0.28 + i * 0.08;
        addPart(g, new THREE.CylinderGeometry(0.018, 0.022, 0.09, 8), mBrass, [t * 0.85, 0.74 + t * 0.76, 0.345 - Math.abs(t) * 0.16], [0.15, 0, -0.78]);
      }
    } else if (variant === "sarge") {
      addPart(g, new THREE.BoxGeometry(0.08, 0.36, 0.04), mAccent, [0, 0.79, 0.335]);
    } else {
      addPart(g, new THREE.BoxGeometry(0.42, 0.34, 0.10), mAccent, [0, 0.78, 0.29]);
    }

    // Belt + Jetpack
    addPart(g, new THREE.CylinderGeometry(0.37, 0.37, 0.08, 18), mLeather, [0, 0.44, 0]);
    addPart(g, new THREE.BoxGeometry(0.11, 0.09, 0.05), mBrass, [0, 0.44, 0.365]);
    addPart(g, new THREE.BoxGeometry(0.36, 0.38, 0.16), mBlack, [0, 0.76, -0.36]);
    for (const sx of [-1, 1]) {
      addPart(g, new THREE.CylinderGeometry(0.085, 0.085, 0.42, 12), mSteel, [sx * 0.12, 0.76, -0.42]);
      addPart(g, new THREE.CylinderGeometry(0.055, 0.085, 0.10, 10), mBlack, [sx * 0.12, 0.52, -0.42]);
      addPart(g, new THREE.CylinderGeometry(0.05, 0.02, 0.06, 10), mGlow, [sx * 0.12, 0.46, -0.42]);
    }

    // Arms & Legs
    for (const sx of [-1, 1]) {
      addPart(g, new THREE.CylinderGeometry(0.09, 0.09, 0.42, 12), mJacket, [sx * 0.45, 0.78, 0], [0, 0, sx * 0.38]);
      addPart(g, new THREE.SphereGeometry(0.10, 10, 8), mBlack, [sx * 0.55, 0.56, 0.02]);
      addPart(g, new THREE.CylinderGeometry(0.13, 0.12, 0.28, 12), mPants, [sx * 0.16, 0.28, 0]);
      addPart(g, new THREE.CylinderGeometry(0.125, 0.13, 0.14, 12), mBlack, [sx * 0.16, 0.10, 0]);
      addPart(g, new THREE.SphereGeometry(0.12, 12, 8), mBlack, [sx * 0.16, 0.06, 0.06], [0, 0, 0], [1, 0.7, 1.4]);
    }
    return g;
  }

  function buildMagnum() {
    const g = new THREE.Group();
    const mChrome = mat(0xdce0eb, 0.95, 0.14);
    const mDark = mat(0x383d45, 0.85, 0.35);
    const mGrip = mat(0x1a1a1c, 0.05, 0.82);
    addPart(g, new THREE.CylinderGeometry(0.052, 0.052, 0.85, 14), mChrome, [0.45, 0.18, 0], [0, 0, Math.PI / 2]);
    addPart(g, new THREE.BoxGeometry(0.80, 0.07, 0.065), mChrome, [0.43, 0.12, 0]);
    addPart(g, new THREE.BoxGeometry(0.82, 0.045, 0.04), mChrome, [0.44, 0.24, 0]);
    addPart(g, new THREE.BoxGeometry(0.40, 0.24, 0.09), mChrome, [-0.10, 0.10, 0]);
    addPart(g, new THREE.CylinderGeometry(0.115, 0.115, 0.22, 16), mChrome, [-0.07, 0.13, 0], [0, 0, Math.PI / 2]);
    addPart(g, new THREE.BoxGeometry(0.17, 0.42, 0.11), mGrip, [-0.36, -0.16, 0], [0, 0, -0.38]);
    addPart(g, new THREE.BoxGeometry(0.06, 0.06, 0.025), mDark, [0.82, 0.28, 0]);
    return g;
  }

  function buildUzi() {
    const g = new THREE.Group();
    const mGun = mat(0x6b737d, 0.85, 0.30);
    const mDark = mat(0x1c1f24, 0.15, 0.72);
    const mSteel = mat(0xb8bfc7, 0.90, 0.22);
    addPart(g, new THREE.BoxGeometry(0.76, 0.20, 0.13), mGun, [0, 0.12, 0]);
    addPart(g, new THREE.BoxGeometry(0.32, 0.11, 0.145), mDark, [0.24, 0.03, 0]);
    addPart(g, new THREE.CylinderGeometry(0.026, 0.026, 0.34, 12), mSteel, [0.58, 0.12, 0], [0, 0, Math.PI / 2]);
    addPart(g, new THREE.BoxGeometry(0.13, 0.30, 0.10), mDark, [-0.06, -0.12, 0], [0, 0, 0.05]);
    addPart(g, new THREE.BoxGeometry(0.085, 0.56, 0.065), mGun, [-0.06, -0.38, 0], [0, 0, 0.05]);
    addPart(g, new THREE.BoxGeometry(0.38, 0.04, 0.11), mSteel, [-0.54, 0.08, 0], [0, 0, 0.12]);
    return g;
  }

  function buildShotgun() {
    const g = new THREE.Group();
    const mSteel = mat(0x333840, 0.85, 0.32);
    const mWood = mat(0x61381f, 0.0, 0.68);
    const mBlack = mat(0x1a1a1c, 0.2, 0.75);
    addPart(g, new THREE.BoxGeometry(0.46, 0.16, 0.11), mSteel, [-0.10, 0.08, 0]);
    addPart(g, new THREE.CylinderGeometry(0.032, 0.032, 1.15, 12), mSteel, [0.68, 0.12, 0], [0, 0, Math.PI / 2]);
    addPart(g, new THREE.CylinderGeometry(0.028, 0.028, 0.96, 12), mBlack, [0.58, 0.05, 0], [0, 0, Math.PI / 2]);
    addPart(g, new THREE.CylinderGeometry(0.052, 0.052, 0.34, 12), mWood, [0.48, 0.05, 0], [0, 0, Math.PI / 2]);
    addPart(g, new THREE.BoxGeometry(0.56, 0.16, 0.095), mWood, [-0.58, -0.02, 0], [0, 0, 0.22]);
    return g;
  }

  function buildRpg() {
    const g = new THREE.Group();
    const mTube = mat(0x857a42, 0.35, 0.52);
    const mWood = mat(0x704221, 0.0, 0.7);
    const mWarhead = mat(0x2e4d26, 0.25, 0.45);
    const mDark = mat(0x26292e, 0.75, 0.4);
    const mTip = mat(0xbfa638, 0.8, 0.3);
    addPart(g, new THREE.CylinderGeometry(0.055, 0.055, 1.55, 14), mTube, [0, 0.08, 0], [0, 0, Math.PI / 2]);
    addPart(g, new THREE.CylinderGeometry(0.070, 0.070, 0.52, 14), mWood, [-0.05, 0.08, 0], [0, 0, Math.PI / 2]);
    addPart(g, new THREE.CylinderGeometry(0.055, 0.105, 0.28, 14), mDark, [-0.88, 0.08, 0], [0, 0, Math.PI / 2]);
    addPart(g, new THREE.BoxGeometry(0.08, 0.22, 0.06), mDark, [0.36, -0.07, 0], [0, 0, -0.22]);
    addPart(g, new THREE.CylinderGeometry(0.115, 0.048, 0.22, 14), mWarhead, [0.86, 0.08, 0], [0, 0, -Math.PI / 2]);
    addPart(g, new THREE.CylinderGeometry(0.025, 0.115, 0.34, 14), mWarhead, [1.14, 0.08, 0], [0, 0, -Math.PI / 2]);
    addPart(g, new THREE.CylinderGeometry(0.012, 0.026, 0.08, 10), mTip, [1.35, 0.08, 0], [0, 0, -Math.PI / 2]);
    return g;
  }

  function buildGrenade() {
    const g = new THREE.Group();
    const mOlive = mat(0x526138, 0.2, 0.52);
    const mYellow = mat(0xebc71f, 0.1, 0.45);
    const mMetal = mat(0xb3bac2, 0.88, 0.25);
    addPart(g, new THREE.SphereGeometry(0.28, 14, 10), mOlive, [0, 0.38, 0], [0, 0, 0], [1, 1.28, 1]);
    addPart(g, new THREE.CylinderGeometry(0.11, 0.15, 0.12, 14), mYellow, [0, 0.74, 0]);
    addPart(g, new THREE.CylinderGeometry(0.09, 0.09, 0.08, 12), mMetal, [0, 0.83, 0]);
    addPart(g, new THREE.BoxGeometry(0.04, 0.40, 0.08), mMetal, [0.24, 0.62, 0], [0, 0, 0.24]);
    addPart(g, new THREE.TorusGeometry(0.09, 0.014, 8, 16), mMetal, [-0.12, 0.88, 0.04], [0, 0.3, 0]);
    return g;
  }

  function buildIsland() {
    const g = new THREE.Group();
    const mRock = mat(0x8f7052, 0.0, 0.92);
    const mGrey = mat(0x6b6661, 0.0, 0.88);
    const mGrass = mat(0x3d8029, 0.0, 0.85);
    const mTuft = mat(0x579e33, 0.0, 0.82);

    const rockGeo = new THREE.ConeGeometry(1.38, 1.9, 10, 3);
    rockGeo.rotateX(Math.PI);
    addPart(g, rockGeo, mRock, [0, -0.62, 0], [0, 0.4, 0], [1.0, 1.0, 0.8]);
    addPart(g, new THREE.CylinderGeometry(1.30, 1.38, 0.22, 12), mGrass, [0, 0.36, 0], [0, 0, 0], [1.0, 1.0, 0.8]);
    addPart(g, new THREE.DodecahedronGeometry(0.32, 0), mGrey, [-0.55, 0.50, 0.22], [0.2, 0.4, 0], [1, 0.65, 0.9]);
    addPart(g, new THREE.DodecahedronGeometry(0.24, 0), mGrey, [0.55, 0.48, -0.18], [0.5, 0.1, 0.3], [1, 0.65, 0.9]);
    for (let i = 0; i < 12; i++) {
      const a = (i / 12) * Math.PI * 2;
      const r = 0.35 + (i % 3) * 0.28;
      addPart(g, new THREE.ConeGeometry(0.05, 0.26, 4), mTuft, [Math.cos(a) * r, 0.54, Math.sin(a) * r * 0.7], [0.1, a, 0.1]);
    }
    return g;
  }

  function buildBush() {
    const g = new THREE.Group();
    const mLeaf = mat(0x297526, 0.0, 0.58, 0x000000, true);
    const mStem = mat(0x24521c, 0.0, 0.75);
    for (let i = 0; i < 8; i++) {
      const a = (i / 8) * Math.PI * 2;
      addPart(g, new THREE.CylinderGeometry(0.016, 0.028, 0.55, 6), mStem, [Math.cos(a) * 0.22, 0.24, Math.sin(a) * 0.22], [0, -a, -0.65]);
      addPart(g, new THREE.SphereGeometry(0.26, 10, 6), mLeaf, [Math.cos(a) * 0.48, 0.42, Math.sin(a) * 0.48], [0, -a, -0.95], [1.1, 0.12, 0.8]);
    }
    return g;
  }

  function buildExplosion() {
    const g = new THREE.Group();
    const mCore = mat(0xfff5a6, 0.0, 0.2, 0xffd959);
    const mMid = mat(0xff7a0f, 0.0, 0.35, 0xf25905);
    const mOuter = mat(0xd9290a, 0.0, 0.5, 0x8c1403);
    addPart(g, new THREE.SphereGeometry(0.36, 12, 10), mCore);
    for (let i = 0; i < 22; i++) {
      const y = 1 - (i / 21) * 2;
      const rad = Math.sqrt(Math.max(0, 1 - y * y));
      const th = i * 2.39996;
      const dir = new THREE.Vector3(Math.cos(th) * rad, y, Math.sin(th) * rad).normalize();
      const len = 0.65 + (i % 4) * 0.18;
      const cone = new THREE.Mesh(new THREE.ConeGeometry(0.14, len, 5), i % 3 === 0 ? mOuter : mMid);
      cone.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), dir);
      cone.position.copy(dir.multiplyScalar(len * 0.45));
      g.add(cone);
    }
    return g;
  }

  return {
    noobie: buildSoldier("noobie"),
    sarge: buildSoldier("sarge"),
    enemy: buildSoldier("enemy"),
    magnum: buildMagnum(),
    uzi: buildUzi(),
    shotgun: buildShotgun(),
    rpg: buildRpg(),
    grenade: buildGrenade(),
    island: buildIsland(),
    bush: buildBush(),
    explosion: buildExplosion()
  };
};
