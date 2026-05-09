<#import "template.ftl" as layout />

<@layout.noauthentication>
    <section>
        <div class="container">
            <h2>Fridge management</h2>

            <#if message??>
                <p class="flash success">${message}</p>
            </#if>
            <#if error??>
                <p class="flash error">${error}</p>
            </#if>

            <div class="grid">
                <section>
                    <h3>Fridges</h3>
                    <h4>Create fridge</h4>
                    <form method="post" action="/fridges" class="form-panel">
                        <label>Name <input type="text" name="name" required></label>
                        <label>Width <input type="number" name="width" min="1" required></label>
                        <label>Height <input type="number" name="height" min="1" required></label>
                        <label>Depth <input type="number" name="depth" min="1" required></label>
                        <button type="submit">Create fridge</button>
                    </form>

                    <h4>Current fridges</h4>
                    <#if fridges?size == 0>
                        <p>No fridges found.</p>
                    <#else>
                        <ul>
                            <#list fridges as fridge>
                                <li>
                                    <a href="/fridges/${fridge.id}">#${fridge.id} - ${fridge.name}</a>
                                    <form method="post" action="/fridges/${fridge.id}/delete" class="inline-form">
                                        <button type="submit">Delete</button>
                                    </form>
                                </li>
                            </#list>
                        </ul>
                    </#if>
                </section>

                <section>
                    <h3>Owners</h3>
                    <h4>Create owner</h4>
                    <form method="post" action="/owners" class="form-panel">
                        <label>Name <input type="text" name="name" required></label>
                        <button type="submit">Create owner</button>
                    </form>

                    <h4>Current owners</h4>
                    <#if owners?size == 0>
                        <p>No owners found.</p>
                    <#else>
                        <ul>
                            <#list owners as owner>
                                <li>
                                    #${owner.id} - ${owner.name}
                                    <form method="post" action="/owners/${owner.id}/delete" class="inline-form">
                                        <button type="submit">Delete</button>
                                    </form>
                                </li>
                            </#list>
                        </ul>
                    </#if>
                </section>

                <section>
                    <h3>Items</h3>
                    <h4>Create item</h4>
                    <form method="post" action="/items" class="form-panel">
                        <label>Name <input type="text" name="name" required></label>
                        <label>Expiry <input type="date" name="expiry" required></label>
                        <label>Owner
                            <select name="ownerId" required>
                                <option value="">Select owner</option>
                                <#list owners as owner>
                                    <option value="${owner.id}">#${owner.id} - ${owner.name}</option>
                                </#list>
                            </select>
                        </label>
                        <button type="submit">Create item</button>
                    </form>

                    <h4>Current items</h4>
                    <#if items?size == 0>
                        <p>No items found.</p>
                    <#else>
                        <ul>
                            <#list items as item>
                                <li>
                                    #${item.id} - ${item.name} (expires ${item.expiryDate?string["yyyy-MM-dd"]}) -
                                    <#if ownerMap[item.ownerId?c]?has_content>
                                        owner #${item.ownerId} - ${ownerMap[item.ownerId?c].name}
                                    <#else>
                                        owner #${item.ownerId}
                                    </#if>
                                    <form method="post" action="/items/${item.id}/delete" class="inline-form">
                                        <button type="submit">Delete</button>
                                    </form>
                                </li>
                            </#list>
                        </ul>
                    </#if>
                </section>
            </div>
        </div>
    </section>
</@layout.noauthentication>
