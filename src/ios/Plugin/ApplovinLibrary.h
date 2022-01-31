//----------------------------------------------------------------------------
// ApplovinLibrary.h
//
// Copyright (c) 2015 Corona Labs. All rights reserved.
//----------------------------------------------------------------------------

#ifndef _ApplovinLibrary_H_
#define _ApplovinLibrary_H_

#include "CoronaLua.h"
#include "CoronaMacros.h"

// This corresponds to the name of the library, e.g. [Lua] require "plugin.library"
// where the '.' is replaced with '_'
CORONA_EXPORT int luaopen_plugin_applovinMax(lua_State *L);

#endif // _ApplovinLibrary_H_
